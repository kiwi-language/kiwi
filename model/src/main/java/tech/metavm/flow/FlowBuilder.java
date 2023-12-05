package tech.metavm.flow;

import tech.metavm.entity.StandardTypes;
import tech.metavm.flow.rest.FlowDTO;
import tech.metavm.object.type.*;
import tech.metavm.object.type.rest.dto.ClassTypeParam;
import tech.metavm.object.type.rest.dto.FieldDTO;
import tech.metavm.object.type.rest.dto.TypeDTO;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlowBuilder {

    public static FlowBuilder newBuilder(ClassType declaringType, String name, String code, FunctionTypeContext functionTypeContext) {
        return new FlowBuilder(declaringType, name, code, functionTypeContext);
    }

    private final FunctionTypeContext functionTypeContext;
    private final ClassType declaringType;
    private final String name;
    private @Nullable
    final String code;
    private Long tmpId;
    private boolean isConstructor;
    private boolean isAbstract;
    private boolean isNative;
    private List<Flow> overriden = new ArrayList<>();
    private FlowDTO flowDTO;
    private Type returnType;
    private List<Parameter> parameters = List.of();
    private PrimitiveType nullType;
    private PrimitiveType voidType;
    private List<TypeVariable> typeParameters = List.of();
    private Flow horizontalTemplate;
    private Flow verticalTemplate;
    private List<Type> typeArguments = List.of();
    private FunctionType type;
    private FunctionType staticType;
    private Flow existing;
    private boolean _static;
    private MetadataState state;

    private FlowBuilder(ClassType declaringType, String name, @Nullable String code, FunctionTypeContext functionTypeContext) {
        this.declaringType = declaringType;
        this.name = name;
        this.code = code;
        this.functionTypeContext = functionTypeContext;
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

    public FlowBuilder overriden(List<Flow> overriden) {
        this.overriden = overriden;
        return this;
    }

    public FlowBuilder flowDTO(FlowDTO flowDTO) {
        this.flowDTO = flowDTO;
        return this;
    }

    public FlowBuilder returnType(Type returnType) {
        this.returnType = returnType;
        return this;
    }

    public FlowBuilder parameters(List<Parameter> parameters) {
        this.parameters = parameters;
        return this;
    }

    public FlowBuilder state(MetadataState state) {
        this.state = state;
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

    public FlowBuilder voidType(PrimitiveType voidType) {
        this.voidType = voidType;
        return this;
    }

    public FlowBuilder horizontalTemplate(Flow horizontalTemplate) {
        this.horizontalTemplate = horizontalTemplate;
        return this;
    }

    public FlowBuilder verticalTemplate(Flow verticalTemplate) {
        this.verticalTemplate = verticalTemplate;
        return this;
    }

    public FlowBuilder type(FunctionType type) {
        this.type = type;
        return this;
    }

    public FlowBuilder staticType(FunctionType staticType) {
        this.staticType = staticType;
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

    public FlowBuilder isStatic(boolean isStatic) {
        this._static = isStatic;
        return this;
    }

    public Flow build() {
        if (returnType == null) {
            if (isConstructor)
                returnType = declaringType;
            else
                returnType = NncUtils.orElse(voidType, StandardTypes::getVoidType);
        }
        var paramTypes = NncUtils.map(parameters, Parameter::getType);
        if (type == null)
            type = functionTypeContext.get(NncUtils.map(parameters, Parameter::getType), returnType);
        if (staticType == null)
            staticType = functionTypeContext.get(NncUtils.prepend(declaringType, paramTypes), returnType);
        var effectiveTmpId = tmpId != null ? tmpId : NncUtils.get(flowDTO, FlowDTO::tmpId);
        if (existing == null) {
            if (state == null)
                state = MetadataState.READY;
            return new Flow(
                    effectiveTmpId,
                    declaringType,
                    name,
                    code,
                    isConstructor,
                    isAbstract,
                    isNative,
                    parameters,
                    returnType,
                    overriden,
                    typeParameters,
                    verticalTemplate,
                    horizontalTemplate,
                    typeArguments,
                    type,
                    staticType,
                    _static,
                    state
            );
        } else {
            existing.setName(name);
            existing.setCode(code);
            existing.setParameters(parameters);
            existing.setReturnType(returnType);
            existing.setOverridden(overriden);
            existing.setTypeParameters(typeParameters);
            existing.setTypeArguments(typeArguments);
            existing.setType(type);
            existing.setStaticType(staticType);
            if (state != null)
                existing.setState(state);
            return existing;
        }
    }

    public static Map<String, Long> getFieldTmpIds(@Nullable TypeDTO typeDTO) {
        if (typeDTO == null) {
            return Map.of();
        }
        var param = (ClassTypeParam) typeDTO.param();
        Map<String, Long> code2tmpId = new HashMap<>();
        for (FieldDTO field : param.fields()) {
            if (field.code() != null) {
                code2tmpId.put(field.code(), field.tmpId());
            }
        }
        return code2tmpId;
    }

    private PrimitiveType getNullType() {
        return NncUtils.orElse(nullType, StandardTypes::getNullType);
    }

    public FlowBuilder existing(Flow existing) {
        this.existing = existing;
        return this;
    }
}
