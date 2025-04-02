package org.metavm.compiler.type;

import org.jetbrains.annotations.NotNull;
import org.metavm.compiler.element.*;
import org.metavm.compiler.syntax.ArrayTypeNode;
import org.metavm.compiler.syntax.TypeNode;
import org.metavm.compiler.util.List;
import org.metavm.object.type.NeverType;
import org.metavm.util.MvOutput;

import javax.annotation.Nullable;

public final class ArrayType implements Type, Comparable<ArrayType> {

    public static final Clazz arrayClass = new Clazz(ClassTag.CLASS, Name.array(),
            Access.PUBLIC, BuiltinClassScope.instance);

    public static final Field lengthField = new Field(
            NameTable.instance.length, PrimitiveType.INT, Access.PUBLIC, false, false, arrayClass
    );

    public static final Method appendMethod = new Method(
            NameTable.instance.append,
            Access.PUBLIC, false, false, false, arrayClass
    );

    static {
        var typeVar = new TypeVar(NameTable.instance.T, PrimitiveType.ANY, arrayClass);
        new Param(NameTable.instance.t, typeVar, appendMethod);
    }

    private final Type elementType;
    private final ClassType classType;
    private final Closure closure = Closure.of(this);

    ArrayType(Type elementType) {
        this.elementType = elementType;
        classType = arrayClass.getInst(List.of(elementType));
    }

    @Override
    public void writeType(ElementWriter writer) {
        writer.writeType(elementType);
        writer.write("[]");
    }

    @Override
    public boolean isAssignableFrom(Type type) {
        return this == type || type instanceof NeverType ||
                type instanceof ArrayType that && elementType.contains(that.elementType);
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
        return classType.getTable();
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
