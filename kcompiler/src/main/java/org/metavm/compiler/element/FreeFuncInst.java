package org.metavm.compiler.element;

import org.metavm.compiler.type.FunctionType;
import org.metavm.compiler.type.Type;
import org.metavm.compiler.type.TypeSubstitutor;
import org.metavm.compiler.type.Types;
import org.metavm.compiler.util.List;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class FreeFuncInst implements FuncInst, Constant {
    private final FreeFunc function;
    private final List<Type> typeArguments;
    private final List<Type> parameterTypes;
    private final Type returnType;
    private final FunctionType type;
    private final Map<List<Type>, FreeFuncInst> instances = new HashMap<>();

    public FreeFuncInst(FreeFunc function, List<Type> typeArguments,
                        List<Type> parameterTypes,
                        Type returnType
    ) {
        this.function = function;
        this.typeArguments = typeArguments;
        this.parameterTypes = parameterTypes;
        this.returnType = returnType;
        this.type = Types.instance.getFunctionType(parameterTypes, returnType);
    }

    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.FUNCTION_REF);
        Elements.writeReference(function, output);
        output.writeList(typeArguments, t -> t.write(output));
    }

    @Override
    public SymName getName() {
        return function.getName();
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitFunctionInst(this);
    }

    @Override
    public void forEachChild(Consumer<Element> action) {

    }

    @Override
    public void write(ElementWriter writer) {
        if (!typeArguments.isEmpty()) {
            writer.write("<");
            writer.writeTypes(typeArguments);
            writer.write(">");
        }
        writer.write(function.getName());
        writer.write("(");
        writer.writeTypes(parameterTypes);
        writer.write(") -> ");
        writer.writeType(returnType);
    }

    @Override
    public FunctionType getType() {
        return type;
    }

    public FreeFunc getFunction() {
        return function;
    }

    @Override
    public FreeFuncInst getInstance(List<Type> typeArguments) {
        if (typeArguments.isEmpty())
            return this;
        var subst = new TypeSubstitutor(this.typeArguments, typeArguments);
        return instances.computeIfAbsent(typeArguments, k ->
                new FreeFuncInst(function, typeArguments,
                        parameterTypes.map(t -> t.accept(subst)),
                        returnType.accept(subst)
                ));
    }

    public List<Type> getTypeArguments() {
        return typeArguments;
    }

    public List<Type> getParameterTypes() {
        return parameterTypes;
    }

    public Type getReturnType() {
        return returnType;
    }

    @Override
    public String toString() {
        return "FreeFunctionInst " + getName();
    }
}
