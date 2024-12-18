package org.metavm.flow;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.Attribute;
import org.metavm.object.type.*;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MethodBuilder {

    public static MethodBuilder newBuilder(Klass declaringType, String name) {
        return new MethodBuilder(declaringType, name);
    }

    private final Klass declaringType;
    private final String name;
    private Long tmpId;
    private boolean isConstructor;
    private boolean isAbstract;
    private boolean isNative;
    private boolean isSynthetic;
    private Access access = Access.PUBLIC;
    private Type returnType;
    private List<NameAndType> parameters = List.of();
    private List<TypeVariable> typeParameters = List.of();
    private boolean hidden;
    private FunctionType staticType;
    private Method existing;
    private boolean _static;
    private @Nullable CodeSource codeSource;
    private final List<Attribute> attributes = new ArrayList<>();
    private MetadataState state;

    private MethodBuilder(Klass declaringType, String name) {
        this.declaringType = declaringType;
        this.name = name;
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

    public MethodBuilder codeSource(CodeSource codeSource) {
        this.codeSource = codeSource;
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

    public MethodBuilder parameters(List<NameAndType> parameters) {
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

    public MethodBuilder parameters(NameAndType... parameters) {
        return parameters(List.of(parameters));
    }

    public MethodBuilder tmpId(Long tmpId) {
        this.tmpId = tmpId;
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
                returnType = NncUtils.orElse(Types.getVoidType(), Types::getVoidType);
        }
        Method method;
        if (existing == null) {
            if (state == null)
                state = MetadataState.READY;
            method = new Method(
                    tmpId,
                    declaringType,
                    name,
                    isConstructor,
                    isAbstract,
                    isNative,
                    isSynthetic,
                    parameters,
                    returnType,
                    typeParameters,
                    _static,
                    access,
                    codeSource,
                    hidden,
                    state
            );
        } else {
            method = existing;
            existing.setName(name);
            existing.setReturnType(returnType);
            existing.setTypeParameters(typeParameters);
            existing.setParameters(NncUtils.map(parameters, p -> new Parameter(null, p.name(), p.type(), method)));
            if (state != null)
                existing.setState(state);
        }
        method.setAttributes(attributes);
        return method;
    }

    public MethodBuilder existing(Method existing) {
        this.existing = existing;
        return this;
    }
}
