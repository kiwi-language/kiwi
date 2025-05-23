package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.MetadataState;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeVariable;
import org.metavm.object.type.Types;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class FunctionBuilder {

    public static FunctionBuilder newBuilder(Id id, String name) {
        return new FunctionBuilder(id, name);
    }

    private final Id id;
    private @NotNull String name;
    private boolean isNative;
    private boolean isSynthetic;
    private List<NameAndType> parameters = new ArrayList<>();
    private @NotNull Type returnType = Types.getVoidType();
    private int returnTypeIndex = -1;
    private List<TypeVariable> typeParameters = new ArrayList<>();
    private MetadataState state = MetadataState.READY;

    public FunctionBuilder(Id id, @NotNull String name) {
        this.id = id;
        this.name = name;
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

    public FunctionBuilder name(String name) {
        this.name = name;
        return this;
    }

    public FunctionBuilder returnType(Type returnType) {
        this.returnType = returnType;
        return this;
    }

    public FunctionBuilder returnTypeIndex(int returnTypeIndex) {
        this.returnTypeIndex = returnTypeIndex;
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
         var func = new Function(
                id,
                name,
                isNative,
                isSynthetic,
                parameters,
                returnTypeIndex,
                typeParameters,
                 state
        );
         if (returnTypeIndex == -1)
             func.setReturnTypeIndex(func.getConstantPool().addValue(returnType));
         return func;
    }

}
