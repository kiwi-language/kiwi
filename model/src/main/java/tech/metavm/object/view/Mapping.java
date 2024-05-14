package tech.metavm.object.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.common.ErrorCode;
import tech.metavm.entity.*;
import tech.metavm.entity.natives.CallContext;
import tech.metavm.entity.natives.ExceptionNative;
import tech.metavm.flow.*;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.type.Klass;
import tech.metavm.object.type.ResolutionStage;
import tech.metavm.object.type.Type;
import tech.metavm.object.view.rest.dto.MappingDTO;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public abstract class Mapping extends Element implements CodeSource, StagedEntity, LoadAware, GenericElement {

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");

    public static final IndexDef<Mapping> IDX_SOURCE_TYPE_TARGET_TYPE
            = IndexDef.createUnique(Mapping.class, "sourceType", "targetType");
    @EntityField("模板")
    @Nullable
    @CopyIgnore
    protected Mapping copySource;

    @EntityField("名称")
    private String name;
    @EntityField("编号")
    @Nullable
    private String code;
    @ChildEntity("源头类型")
    protected final Type sourceType;
    @ChildEntity("目标类型")
    protected final Type targetType;
    @EntityField("映射函数")
    protected @Nullable Method mapper;
    @EntityField("反映射函数")
    protected @Nullable Method unmapper;

    private transient ResolutionStage stage = ResolutionStage.INIT;

    // TODO add NotNull annotation to required parameters
    public Mapping(@Nullable Long tmpId, String name, @Nullable String code, Type sourceType, Type targetType) {
        super(tmpId);
        this.name = NamingUtils.ensureValidName(name);
        this.code = NamingUtils.ensureValidCode(code);
        this.sourceType = addChild(sourceType, "sourceType");
        this.targetType = addChild(targetType, "targetType");
    }

    public DurableInstance mapRoot(DurableInstance instance, CallContext callContext) {
        var view = map(instance, callContext);
        view.accept(new CollectionAwareStructuralVisitor() {

            @Override
            public Void visitArrayInstance(ArrayInstance instance) {
                process(instance);
                return super.visitArrayInstance(instance);
            }

            @Override
            public Void visitClassInstance(ClassInstance instance) {
                process(instance);
                return super.visitClassInstance(instance);
            }

            private void process(DurableInstance instance) {
                var sourceRef = instance.getSourceRef();
                var sourceId = sourceRef.source().tryGetId();
                var mappingKey = sourceRef.getMappingKey();
                boolean isArray = instance instanceof ArrayInstance;
                if (sourceId != null && mappingKey != null) {
                    if (instance.isRoot())
                        instance.initId(new DefaultViewId(isArray, mappingKey, sourceId));
                    else
                        instance.initId(new ChildViewId(isArray, mappingKey, sourceId, (ViewId) instance.getRoot().tryGetId()));
                } else if (/*mappingId != null && */getParent() != null && getParent().tryGetId() != null) {
//                    if(mappingId == null)
//                        mappingId = 0L;
                    if (getParentField() != null)
                        instance.initId(new FieldViewId(isArray, (ViewId) getParent().tryGetId(), mappingKey, getParentField().getTag(), sourceId, instance.getType().toTypeKey()));
                    else
                        instance.initId(new ElementViewId(isArray, (ViewId) getParent().tryGetId(), mappingKey, getIndex(), sourceId, instance.getType().toTypeKey()));
                }
            }
        });
        return view;
    }

    public DurableInstance map(DurableInstance instance, CallContext callContext) {
        var view = (DurableInstance) getMapper().execute(null, List.of(instance), callContext).ret();
        requireNonNull(view).setSourceRef(new SourceRef(instance, (ObjectMapping) this));
        return view;
    }

    public DurableInstance unmap(DurableInstance view, CallContext callContext) {
        if(DebugEnv.debugging) {
            debugLogger.info("unmap {}/{}, idClass: {}, source: {}", Instances.getInstanceDesc(view), view.getStringId(),
                    view.tryGetId() != null ? view.getId().getClass().getName() : null, NncUtils.get(view.tryGetSource(), Instances::getInstanceDesc));
        }
        var result = getUnmapper().execute(null, List.of(view), callContext);
        if(result.exception() != null) {
            var exceptionNative = new ExceptionNative(result.exception());
            throw new BusinessException(ErrorCode.FAIL_TO_SAVE_VIEW, exceptionNative.getMessage().getTitle());
        }
        var source = (DurableInstance) Objects.requireNonNull(result.ret());
        if (source.getContext() == null)
            callContext.instanceRepository().bind(source);
        return source;
    }

    @Override
    public void onLoad(IEntityContext context) {
        this.stage = ResolutionStage.INIT;
    }

    @Override
    public ResolutionStage getStage() {
        return stage;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public String getCode() {
        return code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCode(@Nullable String code) {
        this.code = code;
    }

    public Method getMapper() {
        return requireNonNull(mapper);
    }

    public Method getUnmapper() {
        return requireNonNull(unmapper);
    }

    public void generateCode() {
        generateMappingCode(true);
        generateUnmappingCode(true);
        stage = ResolutionStage.DEFINITION;
    }

    @Override
    public void generateCode(Flow flow) {
        if (flow != mapper && flow != unmapper)
            throw new IllegalArgumentException();
        if (flow == mapper)
            generateMappingCode(false);
        else
            generateUnmappingCode(false);
    }

    public void generateDeclarations() {
        var declaringType = getClassTypeForDeclaration();
        mapper = MethodBuilder
                .newBuilder(declaringType, "映射$" + getQualifiedName(),
                        "map$" + getQualifiedCode())
                .parameters(mapper != null ? mapper.getParameters().get(0) :
                        new Parameter(null, "来源", "source", sourceType))
                .existing(mapper)
                .codeSource(this)
                .isSynthetic(true)
                .isStatic(true)
                .returnType(targetType)
                .build();
        unmapper = MethodBuilder.newBuilder(
                        declaringType, "反映射$" + getQualifiedName(),
                        "unmap$" + getQualifiedCode()
                )
                .existing(unmapper)
                .isSynthetic(true)
                .codeSource(this)
                .isStatic(true)
                .parameters(unmapper != null ? unmapper.getParameters().get(0) :
                        new Parameter(null, "视图", "view", targetType))
                .returnType(sourceType)
                .build();
        stage = ResolutionStage.DECLARATION;
    }

    @Override
    @Nullable
    public Mapping getCopySource() {
        return copySource;
    }

    @Override
    public void setCopySource(@Nullable Object copySource) {
        NncUtils.requireNull(this.copySource);
        this.copySource = (Mapping) copySource;
    }

    protected abstract Klass getClassTypeForDeclaration();

    protected abstract Flow generateMappingCode(boolean generateReadMethod);

    protected abstract Flow generateUnmappingCode(boolean generateWriteMethod);

    public boolean isCodeGenerated() {
        return mapper != null && mapper.isRootScopePresent();
    }

    public Type getSourceType() {
        return sourceType;
    }

    public Type getTargetType() {
        return targetType;
    }

    public abstract MappingDTO toDTO(SerializeContext context);

    public abstract String getQualifiedName();

    public abstract @Nullable String getQualifiedCode();

}
