package org.metavm.flow;

import org.metavm.entity.Attribute;
import org.metavm.entity.StandardTypes;
import org.metavm.flow.rest.FlowDTO;
import org.metavm.object.type.*;
import org.metavm.object.type.rest.dto.ClassTypeParam;
import org.metavm.object.type.rest.dto.FieldDTO;
import org.metavm.object.type.rest.dto.TypeDTO;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodBuilder {

    public static MethodBuilder newBuilder(Klass declaringType, String name, String code) {
        return new MethodBuilder(declaringType, name, code);
    }

    private final Klass declaringType;
    private final String name;
    @Nullable
    private final String code;
    private Long tmpId;
    private boolean isConstructor;
    private boolean isAbstract;
    private boolean isNative;
    private boolean isSynthetic;
    private List<Method> overridden = new ArrayList<>();
    private FlowDTO flowDTO;
    private Access access = Access.PUBLIC;
    private Type returnType;
    private List<Parameter> parameters = List.of();
    private List<TypeVariable> typeParameters = List.of();
    private Method horizontalTemplate;
    private Method verticalTemplate;
    private List<Type> typeArguments = List.of();
    private FunctionType type;
    private boolean hidden;
    private FunctionType staticType;
    private Method existing;
    private boolean _static;
    private @Nullable CodeSource codeSource;
    private final List<Attribute> attributes = new ArrayList<>();
    private MetadataState state;

    private MethodBuilder(Klass declaringType, String name, @Nullable String code) {
        this.declaringType = declaringType;
        this.name = name;
        this.code = code;
    }

    public MethodBuilder isAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
        return this;
    }

    public MethodBuilder isNative(boolean isNative) {
        this.isNative = isNative;
        return this;
    }

    public MethodBuilder isConstructor(boolean isConstructor) {
        this.isConstructor = isConstructor;
        return this;
    }

    public MethodBuilder overridden(List<Method> overridden) {
        this.overridden = overridden;
        return this;
    }

    public MethodBuilder codeSource(CodeSource codeSource) {
        this.codeSource = codeSource;
        return this;
    }

    public MethodBuilder flowDTO(FlowDTO flowDTO) {
        this.flowDTO = flowDTO;
        return this;
    }

    public MethodBuilder access(Access access) {
        this.access = access;
        return this;
    }

    public MethodBuilder isSynthetic(boolean isSynthetic) {
        this.isSynthetic = isSynthetic;
        return this;
    }

    public MethodBuilder returnType(Type returnType) {
        this.returnType = returnType;
        return this;
    }

    public MethodBuilder parameters(List<Parameter> parameters) {
        this.parameters = parameters;
        return this;
    }

    public MethodBuilder hidden(boolean hidden) {
        this.hidden = hidden;
        return this;
    }

    public MethodBuilder state(MetadataState state) {
        this.state = state;
        return this;
    }

    public MethodBuilder parameters(Parameter... parameters) {
        return parameters(List.of(parameters));
    }

    public MethodBuilder tmpId(Long tmpId) {
        this.tmpId = tmpId;
        return this;
    }

    public MethodBuilder horizontalTemplate(Method horizontalTemplate) {
        this.horizontalTemplate = horizontalTemplate;
        return this;
    }

    public MethodBuilder verticalTemplate(Method verticalTemplate) {
        this.verticalTemplate = verticalTemplate;
        return this;
    }

    public MethodBuilder type(FunctionType type) {
        this.type = type;
        return this;
    }

    public MethodBuilder staticType(FunctionType staticType) {
        this.staticType = staticType;
        return this;
    }

    public MethodBuilder typeParameters(List<TypeVariable> typeParameters) {
        this.typeParameters = typeParameters;
        return this;
    }

    public MethodBuilder typeArguments(List<Type> typeArguments) {
        this.typeArguments = typeArguments;
        return this;
    }

    public MethodBuilder isStatic(boolean isStatic) {
        this._static = isStatic;
        return this;
    }

    public MethodBuilder addAttribute(String name, String value) {
        attributes.removeIf(a -> a.name().equals(name));
        attributes.add(new Attribute(name, value));
        return this;
    }

    public Method build() {
        if (returnType == null) {
            if (isConstructor)
                returnType = declaringType.getType();
            else
                returnType = NncUtils.orElse(StandardTypes.getVoidType(), StandardTypes::getVoidType);
        }
        if (NncUtils.isNotEmpty(typeParameters))
            typeArguments = new ArrayList<>(NncUtils.map(typeParameters, TypeVariable::getType));
        if(declaringType.isInterface() && !_static)
            isAbstract = true;
        var effectiveTmpId = tmpId != null ? tmpId : NncUtils.get(flowDTO, FlowDTO::tmpId);
        if(!hidden && !typeArguments.isEmpty())
            hidden = NncUtils.anyMatch(typeArguments, Type::isCaptured);
        Method method;
        if (existing == null) {
            if (state == null)
                state = MetadataState.READY;
            method = new Method(
                    effectiveTmpId,
                    declaringType,
                    name,
                    code,
                    isConstructor,
                    isAbstract,
                    isNative,
                    isSynthetic,
                    parameters,
                    returnType,
                    NncUtils.map(overridden, Method::getRef),
                    typeParameters,
                    typeArguments,
                    _static,
                    horizontalTemplate,
                    access,
                    codeSource,
                    hidden,
                    state
            );
        } else {
            method = existing;
            existing.setName(name);
            existing.setCode(code);
            existing.setParameters(parameters);
            existing.setReturnType(returnType);
            existing.setOverridden(overridden);
            existing.setTypeParameters(typeParameters);
            existing.setTypeArguments(typeArguments);
            existing.setStaticType(staticType);
            if (state != null)
                existing.setState(state);
        }
        method.setAttributes(attributes);
        return method;
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

    public MethodBuilder existing(Method existing) {
        this.existing = existing;
        return this;
    }
}
