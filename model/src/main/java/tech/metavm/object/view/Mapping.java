package tech.metavm.object.view;

import tech.metavm.entity.Element;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.IndexDef;
import tech.metavm.entity.SerializeContext;
import tech.metavm.flow.*;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.core.InstanceRepository;
import tech.metavm.object.type.FunctionTypeProvider;
import tech.metavm.object.type.Type;
import tech.metavm.object.view.rest.dto.MappingDTO;
import tech.metavm.util.NamingUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

import static tech.metavm.util.NamingUtils.escapeTypeName;
import static tech.metavm.util.NamingUtils.tryAddPrefix;

// TODO support generic source type and generic target type
public abstract class Mapping extends Element {

    public static final IndexDef<Mapping> IDX_SOURCE_TYPE_TARGET_TYPE
            = IndexDef.createUnique(Mapping.class, "sourceType", "targetType");

    @EntityField("名称")
    private String name;
    @EntityField("编号")
    @Nullable
    private String code;
    protected final Type sourceType;
    protected final Type targetType;

    protected @Nullable Function mapper;
    protected @Nullable Function unmapper;

    // TODO add NotNull annotation to required parameters
    public Mapping(@Nullable Long tmpId, String name, @Nullable String code, Type sourceType, Type targetType) {
        super(tmpId);
        this.name = NamingUtils.ensureValidName(name);
        this.code = NamingUtils.ensureValidCode(code);
        this.sourceType = sourceType;
        this.targetType = targetType;
    }

    public Instance map(Instance instance, InstanceRepository repository, ParameterizedFlowProvider parameterizedFlowProvider) {
        return getMapper().execute(null, List.of(instance), repository, parameterizedFlowProvider).ret();
    }

    public Instance unmap(Instance view, InstanceRepository repository, ParameterizedFlowProvider parameterizedFlowProvider) {
        return getUnmapper().execute(null, List.of(view), repository, parameterizedFlowProvider).ret();
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
        return Objects.requireNonNull(mapper);
    }

    public Function getUnmapper() {
        return Objects.requireNonNull(unmapper);
    }

    public void generateCode(FunctionTypeProvider functionTypeProvider) {
        generateMappingCode(functionTypeProvider);
        generateUnmappingCode(functionTypeProvider);
    }

    public void generateDeclarations(FunctionTypeProvider functionTypeProvider) {
        mapper = FunctionBuilder
                .newBuilder("映射" + escapeTypeName(getSourceType().getName()),
                        tryAddPrefix(getCode(), "map"), functionTypeProvider)
                .parameters(
                        mapper != null ? mapper.getParameters().get(0) :
                                new Parameter(null, "来源", "source", sourceType))
                .existing(mapper)
                .isSynthetic(true)
                .returnType(targetType)
                .build();
        unmapper = FunctionBuilder
                .newBuilder("反映射" + escapeTypeName(getTargetType().getName()),
                        tryAddPrefix(getCode(), "unmap"), functionTypeProvider)
                .existing(unmapper)
                .isSynthetic(true)
                .parameters(
                        unmapper != null ? unmapper.getParameters().get(0) :
                                new Parameter(null, "视图", "view", targetType))
                .returnType(sourceType)
                .build();
    }

    protected abstract Flow generateMappingCode(FunctionTypeProvider functionTypeProvider);

    protected abstract Flow generateUnmappingCode(FunctionTypeProvider functionTypeProvider);

    public boolean isCodeGenerated() {
        return mapper != null && mapper.getRootScope().isNotEmpty();
    }

    public Type getSourceType() {
        return sourceType;
    }

    public Type getTargetType() {
        return targetType;
    }

    public abstract MappingDTO toDTO(SerializeContext context);

}
