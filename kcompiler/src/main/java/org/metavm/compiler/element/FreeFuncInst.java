package org.metavm.compiler.element;

import org.metavm.compiler.type.FuncType;
import org.metavm.compiler.type.Type;
import org.metavm.compiler.type.Types;
import org.metavm.compiler.util.List;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

import java.util.function.Consumer;

public final class FreeFuncInst extends ElementBase implements FreeFuncRef, FuncInst, Constant {
    private final FreeFunc function;
    private final List<Type> typeArguments;
    private final List<Type> parameterTypes;
    private final Type returnType;
    private final FuncType type;

    public FreeFuncInst(FreeFunc function, List<Type> typeArguments,
                        List<Type> parameterTypes,
                        Type returnType
    ) {
        this.function = function;
        this.typeArguments = typeArguments;
        this.parameterTypes = parameterTypes;
        this.returnType = returnType;
        this.type = Types.instance.getFuncType(parameterTypes, returnType);
    }

    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.FUNCTION_REF);
        Elements.writeReference(function, output);
        output.writeList(typeArguments, t -> t.write(output));
    }

    @Override
    public Name getName() {
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
        writer.write(function.getName());
        writer.write("<");
        writer.writeTypes(typeArguments);
        writer.write(">");
        writer.write("(");
        writer.writeTypes(parameterTypes);
        writer.write(") -> ");
        writer.writeType(returnType);
    }

    @Override
    public FuncType getType() {
        return type;
    }

    public FuncRef getFunc() {
        return function;
    }

    public List<Type> getTypeArgs() {
        return typeArguments;
    }

    public List<Type> getParamTypes() {
        return parameterTypes;
    }

    public Type getRetType() {
        return returnType;
    }

    @Override
    public String toString() {
        return "FreeFunctionInst " + getName();
    }
}
