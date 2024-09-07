package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.object.type.MetadataState;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeVariable;
import org.metavm.object.type.Types;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class FunctionBuilder {

    public static FunctionBuilder newBuilder(String name, @Nullable String code) {
        return new FunctionBuilder(name, code);
    }

    @Nullable
    private Function existing;
    @Nullable
    private Long tmpId;
    private @NotNull String name;
    @Nullable
    private String code;
    private boolean isNative;
    private boolean isSynthetic;
    private List<Parameter> parameters = new ArrayList<>();
    @Nullable
    private Function horizontalTemplate;
    private @NotNull Type returnType = Types.getVoidType();
    private List<TypeVariable> typeParameters = new ArrayList<>();
    private List<? extends Type> typeArguments = new ArrayList<>();
    private CodeSource codeSource;
    private MetadataState state = MetadataState.READY;

    public FunctionBuilder(@NotNull String name, @Nullable String code) {
        this.name = name;
        this.code = code;
    }


    public FunctionBuilder tmpId(Long tmpId) {
        this.tmpId = tmpId;
        return this;
    }

    public FunctionBuilder isNative() {
        this.isNative = true;
        return this;
    }

    public FunctionBuilder isNative(boolean isNative) {
        this.isNative = isNative;
        return this;
    }

    public FunctionBuilder isSynthetic(boolean isSynthetic) {
        this.isSynthetic = isSynthetic;
        return this;
    }

    public FunctionBuilder horizontalTemplate(Function horizontalTemplate) {
        this.horizontalTemplate = horizontalTemplate;
        return this;
    }

    public FunctionBuilder typeArguments(List<?extends Type> typeArguments) {
        this.typeArguments = typeArguments;
        return this;
    }

    public FunctionBuilder existing(Function existing) {
        this.existing = existing;
        return this;
    }

    public FunctionBuilder codeSource(CodeSource codeSource) {
        this.codeSource = codeSource;
        return this;
    }

    public FunctionBuilder name(String name) {
        this.name = name;
        return this;
    }

    public FunctionBuilder code(@Nullable String code) {
        this.code = code;
        return this;
    }

    public FunctionBuilder returnType(Type returnType) {
        this.returnType = returnType;
        return this;
    }

    public FunctionBuilder parameters(Parameter... parameters) {
        return parameters(List.of(parameters));
    }

    public FunctionBuilder parameters(List<Parameter> parameters) {
        this.parameters = parameters;
        return this;
    }

    public FunctionBuilder typeParameters(List<TypeVariable> typeParameters) {
        this.typeParameters = typeParameters;
        return this;
    }

    public FunctionBuilder state(MetadataState state) {
        this.state = state;
        return this;
    }

    public Function build() {
        if (existing == null) {
            return new Function(
                    tmpId,
                    name,
                    code,
                    isNative,
                    isSynthetic,
                    parameters,
                    returnType,
                    typeParameters,
                    typeArguments,
                    horizontalTemplate,
                    codeSource,
                    state
            );
        } else {
            existing.setName(name);
            existing.setCode(code);
            existing.setTypeParameters(typeParameters);
            if (typeParameters.isEmpty())
                existing.setTypeArguments(typeArguments);
            existing.setNative(isNative);
            existing.setParameters(parameters);
            existing.setReturnType(returnType);
            existing.setState(state);
            return existing;
        }
    }

}
