package org.metavm.compiler.type;

import org.jetbrains.annotations.NotNull;
import org.metavm.compiler.element.*;
import org.metavm.compiler.syntax.ArrayTypeNode;
import org.metavm.compiler.syntax.TypeNode;
import org.metavm.util.MvOutput;

import javax.annotation.Nullable;

public final class ArrayType implements Type, Comparable<ArrayType> {

    private static final ElementTable table = new ElementTable();

    static {
        table.add(LengthField.instance);
    }

    private final Type elementType;
    private final Closure closure = Closure.of(this);

    ArrayType(Type elementType) {
        this.elementType = elementType;
    }

    @Override
    public void write(ElementWriter writer) {
        writer.writeType(elementType);
        writer.write("[]");
    }

    @Override
    public boolean isAssignableFrom(Type type) {
        return false;
    }

    @Override
    public <R> R accept(TypeVisitor<R> visitor) {
        return visitor.visitArrayType(this);
    }

    @Override
    public int getTag() {
        return TypeTags.TAG_ARRAY;
    }

    @Override
    public ElementTable getTable() {
        return table;
    }

    @Override
    public String getInternalName(@Nullable Func current) {
        return elementType.getInternalName(current) + "[]";
    }

    @Override
    public TypeNode makeNode() {
        return new ArrayTypeNode(elementType.makeNode());
    }

    @Override
    public Closure getClosure() {
        return closure;
    }

    public Type getElementType() {
        return elementType;
    }

    @Override
    public int compareTo(@NotNull ArrayType o) {
        if (this == o)
            return 0;
        if (o instanceof ArrayType that)
            return Types.instance.compare(elementType, that.elementType);
        else
            return Integer.compare(getTag(), o.getTag());
    }

    @Override
    public void write(MvOutput output) {
        output.write(ConstantTags.ARRAY_TYPE);
        elementType.write(output);
    }
}
