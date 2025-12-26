package org.metavm.entity;

import org.metavm.object.instance.core.EntityReference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.*;
import org.metavm.util.Never;
import org.metavm.util.Null;
import org.metavm.util.Password;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.*;
import java.util.function.*;

public class StdKlassRegistry {

    public static final StdKlassRegistry instance = new StdKlassRegistry();
    private final Map<Class<?>, StdKlassBuilder> builderMap = new HashMap<>();
    private final Map<Class<?>, Klass> klassMap = new HashMap<>();

    private StdKlassRegistry() {
        var builders = ServiceLoader.load(StdKlassBuilder.class, StdKlassRegistry.class.getClassLoader());
        for (StdKlassBuilder builder : builders) {
            builderMap.put(builder.getJavaClass(), builder);
        }
    }

    public void addKlass(Class<?> javaClass, Klass klass) {
        klassMap.put(javaClass, klass);
    }

    public Klass getKlass(Class<?> javaClass) {
        var klass = klassMap.get(javaClass);
        if (klass == null) {
            var builder = Objects.requireNonNull(builderMap.get(javaClass), () -> "No StdKlassBuilder found for class: " + javaClass.getName());
            klass = builder.build(this);
            klassMap.put(javaClass, klass);
        }
        return klass;
    }

    public org.metavm.object.type.Type getType(Type javaType) {
        var type = getPrimitiveType(javaType);
        if (type != null)
            return type;
        if (javaType instanceof Class<?> k) {
            if (Value.class == k || EntityReference.class == k)
                return AnyType.instance;
            if (k.isArray())
                return Types.getArrayType(getType(k.getComponentType()));
        }
        if (javaType instanceof GenericArrayType genericArrayType)
            return Types.getArrayType(getType(genericArrayType.getGenericComponentType()));
        if (javaType instanceof WildcardType wildcardType) {
            org.metavm.object.type.Type lb = NeverType.instance;
            org.metavm.object.type.Type ub = Types.getNullableAnyType();
            if (wildcardType.getLowerBounds().length > 0)
                lb = new IntersectionType(Utils.mapToSet(Arrays.asList(wildcardType.getLowerBounds()), this::getType));
            if (wildcardType.getUpperBounds().length > 0)
                ub = new UnionType(Utils.mapToSet(Arrays.asList(wildcardType.getUpperBounds()), this::getType));
            return new UncertainType(lb, ub);
        }
        return switch (javaType) {
            case ParameterizedType pType -> getParameterizedType(
                    (ClassType) Utils.safeCall(pType.getOwnerType(), this::getType),
                    (Class<?>) pType.getRawType(),
                    Utils.map(pType.getActualTypeArguments(), this::getType)
            );
            case java.lang.reflect.TypeVariable<?> tv -> getTypeVariable(tv.getGenericDeclaration(), tv.getName());
            case Class<?> javaClass -> getKlass(javaClass).getType();
            case null, default ->
                    throw new IllegalArgumentException("Cannot get type for java type '" + javaType.getTypeName() + "'");
        };
    }

    public static @javax.annotation.Nullable org.metavm.object.type.Type getPrimitiveType(java.lang.reflect.Type javaType) {
        if (javaType instanceof Class<?> javaClass) {
            if (javaClass == Object.class)
                return AnyType.instance;
            if (javaClass == Never.class)
                return NeverType.instance;
            if (javaClass == long.class)
                return PrimitiveType.longType;
            if( javaClass == int.class)
                return PrimitiveType.intType;
            if (javaClass == short.class)
                return PrimitiveType.shortType;
            if (javaClass == byte.class)
                return PrimitiveType.byteType;
            if (javaClass == char.class)
                return PrimitiveType.charType;
            if (javaClass == boolean.class)
                return PrimitiveType.booleanType;
            if (javaClass == double.class)
                return PrimitiveType.doubleType;
            if (javaClass == float.class)
                return PrimitiveType.floatType;
            if (javaClass == void.class)
                return PrimitiveType.voidType;
            if (javaClass == Password.class)
                return PrimitiveType.passwordType;
            if (javaClass == Date.class)
                return PrimitiveType.timeType;
            if (javaClass == Null.class)
                return NullType.instance;
        }
        return null;
    }

    public org.metavm.object.type.Type getParameterizedType(@Nullable ClassType ownerType, Class<?> rawClass, List<org.metavm.object.type.Type> typeArgs) {
        if (rawClass == Function.class)
            return Types.getFunctionType(List.of(typeArgs.getFirst()), typeArgs.get(1));
        if (rawClass == BiFunction.class) {
            return Types.getFunctionType(List.of(
                    typeArgs.getFirst(),
                    typeArgs.get(1)
            ), typeArgs.get(2));
        }
        if (rawClass == Consumer.class)
            return Types.getFunctionType(List.of(typeArgs.getFirst()), Types.getVoidType());
        if (rawClass == BiConsumer.class) {
            return Types.getFunctionType(List.of(
                    typeArgs.getFirst(),
                    typeArgs.get(1)
            ), Types.getVoidType());
        }
        if (rawClass == Predicate.class)
            return Types.getFunctionType(List.of(typeArgs.getFirst()), PrimitiveType.booleanType);
        if (rawClass == BiPredicate.class) {
            return Types.getFunctionType(List.of(
                    typeArgs.getFirst(),
                    typeArgs.get(1)
            ), PrimitiveType.booleanType);
        }
        if (rawClass == Runnable.class)
            return Types.getFunctionType(List.of(), Types.getVoidType());
        return new KlassType(ownerType, getKlass(rawClass).getType().getKlass(), typeArgs);
    }

    public VariableType getTypeVariable(java.lang.reflect.GenericDeclaration genericDeclaration, String name) {
        if (genericDeclaration instanceof Class<?> declaringClass)
            return getKlass(declaringClass).getTypeParameterByName(name).getType();
        else
            throw new IllegalArgumentException("Cannot resolve type variable on generic declaration '" + genericDeclaration
                    + "'. Only class type parameters are supported");
    }

    public ClassType getClassType(Class<Klass> javaClass) {
        return (ClassType) getType(javaClass);
    }
}
