package tech.metavm.autograph;

import tech.metavm.dto.RefDTO;
import tech.metavm.flow.Flow;
import tech.metavm.flow.rest.FlowDTO;
import tech.metavm.object.instance.NullInstance;
import tech.metavm.object.meta.*;
import tech.metavm.object.meta.rest.dto.ClassParamDTO;
import tech.metavm.object.meta.rest.dto.FieldDTO;
import tech.metavm.object.meta.rest.dto.TypeDTO;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlowBuilder {

    public static FlowBuilder newBuilder(ClassType declaringType, String name, String code) {
        return new FlowBuilder(declaringType, name, code);
    }

    private final ClassType declaringType;
    private final String name;
    private @Nullable
    final String code;
    private Long tmpId;
    private boolean isConstructor;
    private boolean isAbstract;
    private boolean isNative;
    private Flow overriden;
    private FlowDTO flowDTO;
    private Type outputType;
    private List<Parameter> parameters = List.of();
    private PrimitiveType nullType;
    private List<TypeVariable> typeParameters = List.of();
    private Flow template;
    private List<Type> typeArguments = List.of();

    private ClassType inputType;

    private FlowBuilder(ClassType declaringType, String name, @Nullable String code) {
        this.declaringType = declaringType;
        this.name = name;
        this.code = code;
    }

    public FlowBuilder isAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
        return this;
    }

    public FlowBuilder isNative(boolean isNative) {
        this.isNative = isNative;
        return this;
    }

    public FlowBuilder isConstructor(boolean isConstructor) {
        this.isConstructor = isConstructor;
        return this;
    }

    public FlowBuilder overriden(Flow overriden) {
        this.overriden = overriden;
        return this;
    }

    public FlowBuilder flowDTO(FlowDTO flowDTO) {
        this.flowDTO = flowDTO;
        return this;
    }

    public FlowBuilder outputType(Type returnType) {
        this.outputType = returnType;
        return this;
    }

    public FlowBuilder parameters(List<Parameter> parameters) {
        this.parameters = parameters;
        return this;
    }

    public FlowBuilder parameters(Parameter... parameters) {
        return parameters(List.of(parameters));
    }

    public FlowBuilder tmpId(Long tmpId) {
        this.tmpId = tmpId;
        return this;
    }

    public FlowBuilder nullType(PrimitiveType nullType) {
        this.nullType = nullType;
        return this;
    }

    public FlowBuilder template(Flow template) {
        this.template = template;
        return this;
    }

    public FlowBuilder typeParameters(List<TypeVariable> typeParameters) {
        this.typeParameters = typeParameters;
        return this;
    }

    public FlowBuilder typeArguments(List<Type> typeArguments) {
        this.typeArguments = typeArguments;
        return this;
    }

    public FlowBuilder inputType(ClassType inputType) {
        this.inputType = inputType;
        return this;
    }

    public Flow build() {
        if (this.inputType == null) {
            if (overriden == null) {
                inputType = ClassBuilder
                        .newBuilder("原生流程输入", "NativeFlowInput")
                        .tmpId(NncUtils.get(flowDTO, f -> NncUtils.get(f.inputTypeRef(), RefDTO::tmpId)))
                        .temporary().build();
                Map<String, Long> inputFieldTmpIds = getFieldTmpIds(NncUtils.get(flowDTO, FlowDTO::inputType));
                for (Parameter param : parameters) {
                    createTemporaryField(
                            inputFieldTmpIds.get(param.code()),
                            param.name(),
                            param.code(),
                            inputType,
                            param.type()
                    );
                }
            }
        }
        return new Flow(
                tmpId != null ? tmpId : NncUtils.get(flowDTO, FlowDTO::tmpId),
                declaringType,
                name,
                code,
                isConstructor,
                isAbstract,
                isNative,
                inputType,
                outputType != null ? outputType : StandardTypes.getVoidType(),
                overriden,
                typeParameters,
                template,
                typeArguments
        );
    }

    public static Map<String, Long> getFieldTmpIds(@Nullable TypeDTO typeDTO) {
        if (typeDTO == null) {
            return Map.of();
        }
        var param = (ClassParamDTO) typeDTO.param();
        Map<String, Long> code2tmpId = new HashMap<>();
        for (FieldDTO field : param.fields()) {
            if (field.code() != null) {
                code2tmpId.put(field.code(), field.tmpId());
            }
        }
        return code2tmpId;
    }

    private void createTemporaryField(Long tmpId, String name, String code, ClassType declaringType,
                                      Type type) {
        var field = new Field(
                name, code, declaringType, type, Access.GLOBAL, false, false,
                new NullInstance(nullType()),
                false, false, new NullInstance(nullType()),
                null
        );
        field.setTmpId(tmpId);
    }

    private PrimitiveType nullType() {
        return nullType != null ? nullType : StandardTypes.getNullType();
    }

}
