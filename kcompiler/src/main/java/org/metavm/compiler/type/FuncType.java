package org.metavm.compiler.type;

import org.jetbrains.annotations.NotNull;
import org.metavm.compiler.element.ConstantTags;
import org.metavm.compiler.element.ElementTable;
import org.metavm.compiler.element.ElementWriter;
import org.metavm.compiler.element.Func;
import org.metavm.compiler.syntax.FunctionTypeNode;
import org.metavm.compiler.syntax.TypeNode;
import org.metavm.compiler.util.List;
import org.metavm.util.MvOutput;

import javax.annotation.Nullable;


public final class FuncType implements Type, Comparable<FuncType> {
    private final List<Type> paramTypes;
    private final Type retType;
    private final Closure closure = Closure.of(this);

    FuncType(List<Type> paramTypes, Type retType) {
        this.paramTypes = paramTypes;
        this.retType = retType;
    }

    @Override
    public void writeType(ElementWriter writer) {
        writer.write("(");
        writer.writeTypes(paramTypes);
        writer.write(") -> ");
        writer.writeType(retType);
    }

    @Override
    public boolean isAssignableFrom(Type type) {
        type = type.getUpperBound();
        if (type == this || type == PrimitiveType.NEVER)
            return true;
        if (type instanceof FuncType that) {
            return retType.isAssignableFrom(that.retType)
                    && that.paramTypes.matches(this.paramTypes, Type::isAssignableFrom);
        } else
            return false;
    }

    @Override
    public <R> R accept(TypeVisitor<R> visitor) {
        return visitor.visitFunctionType(this);
    }

    @Override
    public int getTag() {
        return TypeTags.TAG_FUNCTION;
    }

    @Override
    public String getInternalName(@Nullable Func current) {
        return "(" +
                paramTypes.map(t -> t.getInternalName(current)).join(",") + ")->"
                + retType.getInternalName(current)
                ;
    }

    @Override
    public TypeNode makeNode() {
        var node = new FunctionTypeNode(paramTypes.map(Type::makeNode), retType.makeNode());
        node.setType(this);
        return node;
    }

    @Override
    public Closure getClosure() {
        return closure;
    }

    @Override
    public int compareTo(@NotNull FuncType o) {
        if (this == o)
            return 0;
        var r = Types.instance.compare(retType, o.retType);
        if (r != 0)
            return r;
        return Types.instance.compareTypes(o.paramTypes, paramTypes);
    }

    public List<Type> getParamTypes() {
        return paramTypes;
    }

    public Type getRetType() {
        return retType;
    }

    @Override
    public String toString() {
        return "FunctionType[" +
                "parameterTypes=" + paramTypes + ", " +
                "returnType=" + retType + ']';
    }

    @Override
    public void write(MvOutput output) {
        output.write(ConstantTags.FUNCTION_TYPE);
        output.writeList(paramTypes, t -> t.write(output));
        retType.write(output);
    }

    @Override
    public ElementTable getTable() {
        return ElementTable.empty;
    }
}
