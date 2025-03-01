package org.metavm.compiler.type;

import org.metavm.compiler.util.List;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public class Types {

    public static final Types instance = new Types();

    private final Map<List<Type>, FunctionType> functionTypes = new HashMap<>();
    private final Map<List<Type>, UnionType> unionTypes = new HashMap<>();
    private final Map<List<Type>, IntersectionType> intersectionType = new HashMap<>();
    private final Map<List<Type>, UncertainType> uncertainTypes = new HashMap<>();
    private final Map<Type, ArrayType> arrayTypes = new IdentityHashMap<>();

    private Types() {
    }

    public Type substitute(Type type, List<Type> from, List<Type> to) {
        var subst = new TypeSubstitutor(from, to);
        return type.accept(subst);
    }

    public FunctionType getFunctionType(List<Type> parameterTypes, Type returnType) {
        return functionTypes.computeIfAbsent(
                parameterTypes.prepend(returnType),
                k -> new FunctionType(parameterTypes, returnType)
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
        return getNullableType(PrimitiveType.STRING);
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
