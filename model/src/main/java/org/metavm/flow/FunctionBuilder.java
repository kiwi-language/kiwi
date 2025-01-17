package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.object.type.MetadataState;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeVariable;
import org.metavm.object.type.Types;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class FunctionBuilder {

    public static FunctionBuilder newBuilder(String name) {
        return new FunctionBuilder(name);
    }

    @Nullable
    private Function existing;
    @Nullable
    private Long tmpId;
    private @NotNull String name;
    private boolean isNative;
    private boolean isSynthetic;
    private List<NameAndType> parameters = new ArrayList<>();
    private @NotNull Type returnType = Types.getVoidType();
    private List<TypeVariable> typeParameters = new ArrayList<>();
    private CodeSource codeSource;
    private MetadataState state = MetadataState.READY;

    public FunctionBuilder(@NotNull String name) {
        this.name = name;
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

    public FunctionBuilder returnType(Type returnType) {
        this.returnType = returnType;
        return this;
    }

    public FunctionBuilder parameters(NameAndType... parameters) {
        return parameters(List.of(parameters));
    }

    public FunctionBuilder parameters(List<NameAndType> parameters) {
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
                    isNative,
                    isSynthetic,
                    parameters,
                    returnType,
                    typeParameters,
                     codeSource,
                    state
            );
        } else {
            existing.setName(name);
            existing.setTypeParameters(typeParameters);
            existing.setNative(isNative);
            existing.setReturnType(returnType);
            existing.setState(state);
            existing.setParameters(Utils.map(parameters, p -> new Parameter(null, p.name(), p.type(), existing)));
            return existing;
        }
    }

}
