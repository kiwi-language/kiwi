package tech.metavm.object.view;

import tech.metavm.entity.*;
import tech.metavm.flow.*;
import tech.metavm.object.instance.core.StructuralVisitor;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.type.FunctionTypeProvider;
import tech.metavm.object.type.ResolutionStage;
import tech.metavm.object.type.Type;
import tech.metavm.object.view.rest.dto.MappingDTO;
import tech.metavm.util.NamingUtils;

import javax.annotation.Nullable;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static tech.metavm.util.NamingUtils.escapeTypeName;
import static tech.metavm.util.NamingUtils.tryAddPrefix;

// TODO support generic source type and generic target type
public abstract class Mapping extends Element implements CodeSource, StagedEntity, LoadAware {

    public static final IndexDef<Mapping> IDX_SOURCE_TYPE_TARGET_TYPE
            = IndexDef.createUnique(Mapping.class, "sourceType", "targetType");

    @EntityField("名称")
    private String name;
    @EntityField("编号")
    @Nullable
    private String code;
    protected final Type sourceType;
    protected final Type targetType;

    @ChildEntity("映射函数")
    protected @Nullable Function mapper;
    @ChildEntity("反映射函数")
    protected @Nullable Function unmapper;

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
        view.accept(new StructuralVisitor() {
            @Override
            public Void visitDurableInstance(DurableInstance instance) {
                var sourceRef = instance.getSourceRef();
                var sourceId = sourceRef.source().getId();
                var mappingId = sourceRef.mapping().tryGetId();
                if (sourceId != null && mappingId != null) {
                    if (instance.isRoot())
                        instance.initId(new ViewId(mappingId, sourceId));
                    else
                        instance.initId(new ChildViewId(mappingId, sourceId, (ViewId) instance.getRoot().getId()));
                }
                return super.visitDurableInstance(instance);
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
        return (DurableInstance) getUnmapper().execute(null, List.of(view), repository, parameterizedFlowProvider).ret();
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

    public Function getMapper() {
        return requireNonNull(mapper);
    }

    public Function getUnmapper() {
        return requireNonNull(unmapper);
    }

    public void generateCode(FunctionTypeProvider functionTypeProvider) {
        generateMappingCode(functionTypeProvider);
        generateUnmappingCode(functionTypeProvider);
        stage = ResolutionStage.DEFINITION;
    }

    @Override
    public void generateCode(Flow flow, FunctionTypeProvider functionTypeProvider) {
        if (flow != mapper && flow != unmapper)
            throw new IllegalArgumentException();
        if (flow == mapper)
            generateMappingCode(functionTypeProvider);
        else
            generateUnmappingCode(functionTypeProvider);
    }

    public void generateDeclarations(FunctionTypeProvider functionTypeProvider) {
        mapper = addChild(FunctionBuilder
                        .newBuilder("映射" + escapeTypeName(targetType.getName()),
                                tryAddPrefix(targetType.getCode(), "map"), functionTypeProvider)
                        .parameters(mapper != null ? mapper.getParameters().get(0) :
                                new Parameter(null, "来源", "source", sourceType))
                        .existing(mapper)
                        .codeSource(this)
                        .isSynthetic(true)
                        .returnType(targetType)
                        .build(),
                "mapper");
        unmapper = addChild(FunctionBuilder
                        .newBuilder("反映射" + escapeTypeName(targetType.getName()),
                                tryAddPrefix(targetType.getCode(), "unmap"), functionTypeProvider)
                        .existing(unmapper)
                        .isSynthetic(true)
                        .codeSource(this)
                        .parameters(unmapper != null ? unmapper.getParameters().get(0) :
                                new Parameter(null, "视图", "view", targetType))
                        .returnType(sourceType)
                        .build(),
                "unmapper");
        stage = ResolutionStage.DECLARATION;
    }

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

}
