package org.metavm.compiler.type;

import org.metavm.compiler.element.*;
import org.metavm.compiler.util.List;
import org.metavm.object.type.SymbolRefs;
import org.metavm.util.MvOutput;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import static org.metavm.util.WireTypes.CLASS_TYPE;

public class Types {

    public static final Types instance = new Types();

    private final Map<List<Type>, FuncType> functionTypes = new HashMap<>();
    private final Map<List<Type>, UnionType> unionTypes = new HashMap<>();
    private final Map<List<Type>, IntersectionType> intersectionType = new HashMap<>();
    private final Map<List<Type>, UncertainType> uncertainTypes = new HashMap<>();
    private final Map<Type, ArrayType> arrayTypes = new IdentityHashMap<>();
    private final Clazz stringClass = createStringClass();

    private Types() {
    }

    private Clazz createStringClass() {
        var clazz = new Clazz(ClassTag.CLASS, NameTable.instance.string, Access.PUBLIC, BuiltinClassScope.instance) {

            @Override
            public Name getQualName() {
                return NameTable.instance.qualString;
            }

            @Override
            public void write(MvOutput output) {
                output.write(CLASS_TYPE);
                output.write(SymbolRefs.KLASS);
                output.writeUTF("java.lang.String");
            }

        };
        var isEmpty = new Method("isEmpty", Access.PUBLIC, false, false, false, clazz);
        isEmpty.setRetType(PrimitiveType.BOOL);

        var equals = new Method("equals", Access.PUBLIC, false, false, false, clazz);
        new Param("o", getNullableAny(), equals);
        equals.setRetType(PrimitiveType.BOOL);

        var hashCode = new Method("hashCode", Access.PUBLIC, false, false, false, clazz);
        hashCode.setRetType(PrimitiveType.INT);

        var length = new Method("length", Access.PUBLIC, false, false, false, clazz);
        length.setRetType(PrimitiveType.INT);

        var substr = new Method("substring", Access.PUBLIC, false, false, false, clazz);
        new Param("from", PrimitiveType.INT, substr);
        substr.setRetType(clazz);

        var substr1 = new Method("substring", Access.PUBLIC, false, false, false, clazz);
        new Param("from", PrimitiveType.INT, substr1);
        new Param("to", PrimitiveType.INT, substr1);
        substr1.setRetType(clazz);

        var compareTo = new Method("compareTo", Access.PUBLIC, false, false, false, clazz);
        new Param("that", getNullableType(clazz), compareTo);
        compareTo.setRetType(PrimitiveType.INT);

        return clazz;
    }

    public Type substitute(Type type, List<Type> from, List<Type> to) {
        var subst = new TypeSubst(from, to);
        return type.accept(subst);
    }

    public FuncType getFuncType(List<Type> parameterTypes, Type returnType) {
        return functionTypes.computeIfAbsent(
                parameterTypes.prepend(returnType),
                k -> new FuncType(parameterTypes, returnType)
        );
    }

    public UnionType getUnionType(List<Type> alternatives) {
        return unionTypes.computeIfAbsent(sorted(alternatives), UnionType::new);
    }

    public Type getIntersectionType(List<Type> bounds) {
        return intersectionType.computeIfAbsent(sorted(bounds), IntersectionType::new);
    }

    public ArrayType getArrayType(Type type) {
        return arrayTypes.computeIfAbsent(type, ArrayType::new);
    }

    public UncertainType getUncertainType(Type lowerBound, Type upperBound) {
        return uncertainTypes.computeIfAbsent(
                List.of(lowerBound, upperBound),
                k -> new UncertainType(lowerBound, upperBound)
        );
    }

    public UncertainType getUpperBoundedType(Type upperBound) {
        return getUncertainType(PrimitiveType.NEVER, upperBound);
    }

    // Type list are usually short, so this implementation is fine
    public List<Type> sorted(List<Type> types) {
        List<Type> sorted = List.nil();
        for (Type type : types) {
            sorted = insert(sorted, type);
        }
        return sorted;
    }

    public Type getUpperBound(Type type) {
        if (type instanceof UncertainType uncertainType)
            return uncertainType.getUpperBound();
        else
            return type;
    }

    private List<Type> insert(List<Type> types, Type type) {
        if (types.isEmpty() || compare(types.head(), type) >= 0)
            return new List<>(type, types);
        else
            return new List<>(types.head(), insert(types.tail(), type));
    }

    public int compare(Type t1, Type t2) {
        var r = Integer.compare(t1.getTag(), t2.getTag());
        if (r != 0)
            return r;
        //noinspection unchecked,rawtypes
        return ((Comparable) t1).compareTo(t2);
    }

    public int compareTypes(List<Type> types1, List<Type> types) {
        return List.compare(types1, types, this::compare);
    }

    public Type getNullableType(Type type) {
        return getUnionType(List.of(type, PrimitiveType.NULL));
    }

    public Type getNullableAny() {
        return getNullableType(PrimitiveType.ANY);
    }

    public Type getNullableString() {
        return getNullableType(getStringType());
    }

    public ClassType getStringType() {
        return stringClass;
    }

    public Clazz getStringClass() {
        return stringClass;
    }

    public static Type getLUB(List<Type> types) {
        if (types.isEmpty())
            return PrimitiveType.NEVER;

        for (Type type : types) {
            if (types.allMatch(type::isAssignableFrom))
                return type;
        }

        var it = types.iterator();
        var cl = it.next().getClosure();
        while (it.hasNext())
            cl = cl.intersection(it.next().getClosure());
        if (cl.isEmpty())
            return PrimitiveType.ANY;
        return cl.toType();
    }

}
