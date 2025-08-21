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

    public static final Clazz arrayClass = ClazzBuilder.newBuilder(Name.array(), BuiltinClassScope.instance)
            .addTypeParam(NameTable.instance.T)
            .build();

    public static final Clazz intArrayClass = ClazzBuilder.newBuilder(NameTable.instance.intArray, BuiltinClassScope.instance)
            .interfaces(List.of(arrayClass.getInst(List.of(PrimitiveType.INT))))
            .build();

    public static final Clazz longArrayClass = ClazzBuilder.newBuilder(NameTable.instance.longArray, BuiltinClassScope.instance)
            .interfaces(List.of(arrayClass.getInst(List.of(PrimitiveType.LONG))))
            .build();

    public static final Clazz floatArrayClass = ClazzBuilder.newBuilder(NameTable.instance.floatArray, BuiltinClassScope.instance)
            .interfaces(List.of(arrayClass.getInst(List.of(PrimitiveType.FLOAT))))
            .build();

    public static final Clazz doubleArrayClass = ClazzBuilder.newBuilder(NameTable.instance.doubleArray, BuiltinClassScope.instance)
            .interfaces(List.of(arrayClass.getInst(List.of(PrimitiveType.DOUBLE))))
            .build();

    public static final Field lengthField = new Field(
            NameTable.instance.length, PrimitiveType.INT, Access.PUBLIC, false, false, false, arrayClass
    );

    public static final Method appendMethod = MethodBuilder.newBuilder(arrayClass, NameTable.instance.append)
            .addParam(NameTable.instance.t, arrayClass.getTypeParams().head())
            .build();

    public static final Method removeMethod = MethodBuilder.newBuilder(arrayClass, NameTable.instance.remove)
            .addParam(NameTable.instance.t, arrayClass.getTypeParams().head())
            .retType(PrimitiveType.BOOL)
            .build();

    public static final Method forEachMethod = MethodBuilder.newBuilder(arrayClass, NameTable.instance.forEach)
            .addParam(
                    NameTable.instance.action,
                    Types.instance.getFuncType(List.of(arrayClass.getTypeParams().head()), PrimitiveType.VOID)
            )
            .build();

    public static final Method sortMethod = MethodBuilder.newBuilder(arrayClass, NameTable.instance.sort)
            .addParam(
                    NameTable.instance.comparator,
                    Types.instance.getFuncType(List.of(arrayClass.getTypeParams().head(), arrayClass.getTypeParams().head()), PrimitiveType.INT)
            )
            .build();

    public static final Method reverseMethod = MethodBuilder.newBuilder(arrayClass, NameTable.instance.reverse).build();

    public static final Method mapMethod = MethodBuilder.newBuilder(arrayClass, NameTable.instance.map).build();

    static {
        var typeVar2 = new TypeVar(NameTable.instance.R, PrimitiveType.ANY, mapMethod);
        new Param(
                NameTable.instance.mapper,
                Types.instance.getFuncType(List.of(arrayClass.getTypeParams().head()), typeVar2),
                mapMethod
        );
        mapMethod.setRetType(Types.instance.getArrayType(typeVar2));
    }

    public static final Method intSumMethod = MethodBuilder.newBuilder(intArrayClass, NameTable.instance.sum)
            .retType(PrimitiveType.INT)
            .build();

    public static final Method longSumMethod = MethodBuilder.newBuilder(longArrayClass, NameTable.instance.sum)
            .retType(PrimitiveType.LONG)
            .build();

    public static final Method floatSumMethod = MethodBuilder.newBuilder(floatArrayClass, NameTable.instance.sum)
            .retType(PrimitiveType.FLOAT)
            .build();

    public static final Method doubleSmMethod = MethodBuilder.newBuilder(doubleArrayClass, NameTable.instance.sum)
            .retType(PrimitiveType.DOUBLE)
            .build();

    private final Type elementType;
    private final ClassType classType;
    private final Closure closure = Closure.of(this);

    ArrayType(Type elementType) {
        this.elementType = elementType;
        if (elementType instanceof PrimitiveType primitiveType) {
            classType = switch (primitiveType) {
                case INT -> intArrayClass;
                case LONG -> longArrayClass;
                case FLOAT -> floatArrayClass;
                case DOUBLE -> doubleArrayClass;
                default -> arrayClass.getInst(List.of(elementType));
            };
        }
        else
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
        var node = new ArrayTypeNode(elementType.makeNode());
        node.setType(this);
        return node;
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
