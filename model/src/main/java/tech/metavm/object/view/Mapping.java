package tech.metavm.object.view;

import tech.metavm.common.ErrorCode;
import tech.metavm.entity.*;
import tech.metavm.entity.natives.ExceptionNative;
import tech.metavm.flow.*;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.type.*;
import tech.metavm.object.view.rest.dto.MappingDTO;
import tech.metavm.util.BusinessException;
import tech.metavm.util.NamingUtils;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public abstract class Mapping extends Element implements CodeSource, StagedEntity, LoadAware, GenericElement {

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
    @EntityField("源头类型")
    protected final Type sourceType;
    @EntityField("目标类型")
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
        this.sourceType = sourceType;
        this.targetType = targetType;
    }

    public DurableInstance mapRoot(DurableInstance instance, InstanceRepository repository, ParameterizedFlowProvider parameterizedFlowProvider) {
        var view = map(instance, repository, parameterizedFlowProvider);
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
                var sourceId = sourceRef.source().getId();
                var mappingId = NncUtils.get(sourceRef.getMappingId(), Id::parse);
                if (sourceId != null && mappingId != null) {
                    if (instance.isRoot())
                        instance.initId(new DefaultViewId(mappingId, sourceId));
                    else
                        instance.initId(new ChildViewId(mappingId, sourceId, (ViewId) instance.getRoot().getId()));
                } else if (/*mappingId != null && */getParent() != null && getParent().getId() != null) {
//                    if(mappingId == null)
//                        mappingId = 0L;
                    if (getParentField() != null)
                        instance.initId(new FieldViewId((ViewId) getParent().getId(), mappingId, getParentField().getId(), sourceId, instance.getType().getEntityId()));
                    else
                        instance.initId(new ElementViewId((ViewId) getParent().getId(), mappingId, getIndex(), sourceId, instance.getType().getEntityId()));
                }
            }
        });
        return view;
    }

    public DurableInstance map(DurableInstance instance, InstanceRepository repository, ParameterizedFlowProvider parameterizedFlowProvider) {
        var view = (DurableInstance) getMapper().execute(null, List.of(instance), repository, parameterizedFlowProvider).ret();
        requireNonNull(view).setSourceRef(new SourceRef(instance, this));
        return view;
    }

    public DurableInstance unmap(DurableInstance view, InstanceRepository repository, ParameterizedFlowProvider parameterizedFlowProvider) {
        var result = getUnmapper().execute(null, List.of(view), repository, parameterizedFlowProvider);
        if(result.exception() != null) {
            var exceptionNative = new ExceptionNative(result.exception());
            throw new BusinessException(ErrorCode.FAIL_TO_SAVE_VIEW, exceptionNative.getMessage().getTitle());
        }
        var source = (DurableInstance) Objects.requireNonNull(result.ret());
        if (source.getContext() == null)
            repository.bind(source);
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

    public void generateCode(CompositeTypeFacade compositeTypeFacade) {
        generateMappingCode(compositeTypeFacade);
        generateUnmappingCode(compositeTypeFacade);
        stage = ResolutionStage.DEFINITION;
    }

    @Override
    public void generateCode(Flow flow, CompositeTypeFacade compositeTypeFacade) {
        if (flow != mapper && flow != unmapper)
            throw new IllegalArgumentException();
        if (flow == mapper)
            generateMappingCode(compositeTypeFacade);
        else
            generateUnmappingCode(compositeTypeFacade);
    }

    public void generateDeclarations(CompositeTypeFacade compositeTypeFacade) {
        var declaringType = getClassTypeForDeclaration();
        mapper = MethodBuilder
                .newBuilder(declaringType, "映射$" + getQualifiedName(),
                        "map$" + getQualifiedCode(), compositeTypeFacade)
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
                        "unmap$" + getQualifiedCode(),
                        compositeTypeFacade)
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

    protected abstract ClassType getClassTypeForDeclaration();

    protected abstract Flow generateMappingCode(FunctionTypeProvider functionTypeProvider);

    protected abstract Flow generateUnmappingCode(FunctionTypeProvider functionTypeProvider);

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
