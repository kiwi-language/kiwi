package org.metavm.object.view;

import org.metavm.api.EntityType;
import org.metavm.common.ErrorCode;
import org.metavm.entity.*;
import org.metavm.entity.natives.CallContext;
import org.metavm.entity.natives.ExceptionNative;
import org.metavm.flow.*;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.Klass;
import org.metavm.object.type.ResolutionStage;
import org.metavm.object.type.Type;
import org.metavm.object.view.rest.dto.MappingDTO;
import org.metavm.util.*;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

@EntityType
public abstract class Mapping extends Element implements CodeSource, StagedEntity, LoadAware, GenericElement {

    @Nullable
    @CopyIgnore
    protected Mapping copySource;
    private String name;
    @Nullable
    private String code;
    protected final Type sourceType;
    protected final Type targetType;
    protected @Nullable Method mapper;
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

    public DurableInstance mapRoot(DurableInstance instance, CallContext callContext) {
        var view = map(instance, callContext);
        view.accept(new CollectionAwareStructuralVisitor() {

            @Override
            public void visitDurableInstance(DurableInstance instance) {
                var sourceRef = instance.getSourceRef();
                var sourceId = sourceRef.source().tryGetId();
                var mappingKey = sourceRef.getMappingKey();
                boolean isArray = instance instanceof ArrayInstance;
                if (sourceId != null && mappingKey != null) {
                    if (instance.isRoot())
                        instance.initId(new DefaultViewId(isArray, mappingKey, sourceId));
                    else
                        instance.initId(new ChildViewId(isArray, mappingKey, sourceId, (ViewId) instance.getRoot().tryGetId()));
                } else if (/*mappingId != null && */instance.getParent() != null && instance.getParent().tryGetId() != null) {
//                    if(mappingId == null)
//                        mappingId = 0L;
                    if (instance.getParentField() != null)
                        instance.initId(new FieldViewId(isArray, (ViewId) instance.getParent().tryGetId(), mappingKey, instance.getParentField().getTagId(), sourceId, instance.getType().toTypeKey()));
                    else {
                        var index = ((ArrayInstance) instance.getParent()).getElements().indexOf(instance.getReference());
                        instance.initId(new ElementViewId(isArray, (ViewId) instance.getParent().tryGetId(), mappingKey, index, sourceId, instance.getType().toTypeKey()));
                    }
                }
                super.visitDurableInstance(instance);
            }
        });
//        view.accept(new CollectionAwareStructuralVisitor() {
//
//            @Override
//            public Void visitArrayInstance(ArrayInstance instance) {
//                process(instance);
//                return super.visitArrayInstance(instance);
//            }
//
//            @Override
//            public Void visitClassInstance(ClassInstance instance) {
//                process(instance);
//                return super.visitClassInstance(instance);
//            }
//
//            private void process(DurableInstance instance) {
//                var sourceRef = instance.getSourceRef();
//                var sourceId = sourceRef.source().tryGetId();
//                var mappingKey = sourceRef.getMappingKey();
//                boolean isArray = instance instanceof ArrayInstance;
//                if (sourceId != null && mappingKey != null) {
//                    if (instance.isRoot())
//                        instance.initId(new DefaultViewId(isArray, mappingKey, sourceId));
//                    else
//                        instance.initId(new ChildViewId(isArray, mappingKey, sourceId, (ViewId) instance.getRoot().tryGetId()));
//                } else if (/*mappingId != null && */getParent() != null && getParent().tryGetId() != null) {
////                    if(mappingId == null)
////                        mappingId = 0L;
//                    if (getParentField() != null)
//                        instance.initId(new FieldViewId(isArray, (ViewId) getParent().tryGetId(), mappingKey, getParentField().getTagId(), sourceId, instance.getType().toTypeKey()));
//                    else
//                        instance.initId(new ElementViewId(isArray, (ViewId) getParent().tryGetId(), mappingKey, getIndex(), sourceId, instance.getType().toTypeKey()));
//                }
//            }
//        });
        return view;
    }

    public DurableInstance map(DurableInstance instance, CallContext callContext) {
        var view = (InstanceReference) getMapper().execute(null, List.of(instance.getReference()), callContext).ret();
        requireNonNull(view).resolve().setSourceRef(new SourceRef(instance.getReference(), (ObjectMapping) this));
        return view.resolve();
    }

    public InstanceReference unmap(InstanceReference view, CallContext callContext) {
        if(DebugEnv.debugging) {
            DebugEnv.logger.info("unmap {}/{}, idClass: {}, source: {}", Instances.getInstanceDesc(view), view.getStringId(),
                    view.tryGetId() != null ? view.getId().getClass().getName() : null, NncUtils.get(view.resolve().tryGetSource(), Instances::getInstanceDesc));
        }
        var result = getUnmapper().execute(null, List.of(view), callContext);
        if(result.exception() != null) {
            var exceptionNative = new ExceptionNative(result.exception());
            throw new BusinessException(ErrorCode.FAIL_TO_SAVE_VIEW, exceptionNative.getMessage().getTitle());
        }
        var source = (InstanceReference) Objects.requireNonNull(result.ret());
        if (source.resolve().getContext() == null)
            callContext.instanceRepository().bind(source.resolve());
        return source;
    }

    @Override
    public void onLoad() {
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
            throw new IllegalArgumentException("Flow " + flow + " is neither the mapper nor the unmapper of this Mapping" +
                    ", mapper: " + mapper + ", unmapper: " + unmapper);
        if (flow == mapper)
            generateMappingCode(false);
        else
            generateUnmappingCode(false);
    }

    public void generateDeclarations() {
        var declaringType = getClassTypeForDeclaration();
        mapper = MethodBuilder
                .newBuilder(declaringType, "map$" + getQualifiedName(),
                        "map$" + getQualifiedCode())
                .parameters(mapper != null ? mapper.getParameters().get(0) :
                        new Parameter(null, "source", "source", sourceType))
                .existing(mapper)
                .codeSource(this)
                .isSynthetic(true)
                .isStatic(true)
                .returnType(targetType)
                .build();
        unmapper = MethodBuilder.newBuilder(
                        declaringType, "unmap$" + getQualifiedName(),
                        "unmap$" + getQualifiedCode()
                )
                .existing(unmapper)
                .isSynthetic(true)
                .codeSource(this)
                .isStatic(true)
                .parameters(unmapper != null ? unmapper.getParameters().get(0) :
                        new Parameter(null, "view", "view", targetType))
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
