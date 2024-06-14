package org.metavm.util;

import java.lang.reflect.*;

public abstract class TypeTransformer {

    public Type transformType(Type  type) {
        return switch (type) {
            case Class<?> klass -> transformClass(klass);
            case ParameterizedType pType -> transformParameterizedType(pType);
            case TypeVariable<?> typeVar -> transformTypeVariable(typeVar);
            case WildcardType wildcardType -> transformWildcardType(wildcardType);
            case GenericArrayType genericArrayType -> transformGenericArrayType(genericArrayType);
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }

    public Type transformClass(Class<?> klass) {
        return klass;
    }

    public Type transformParameterizedType(ParameterizedType parameterizedType) {
        return new ParameterizedTypeImpl(
                NncUtils.get(parameterizedType.getOwnerType(), this::transformType),
                (Class<?>) transformType(parameterizedType.getRawType()),
                NncUtils.mapArray(parameterizedType.getActualTypeArguments(), this::transformType, Type[]::new)
        );
    }

    public Type transformTypeVariable(TypeVariable<?> typeVariable) {
        return typeVariable;
    }

    public Type transformWildcardType(WildcardType wildcardType) {
        return new WildcardTypeImpl(
                NncUtils.mapArray(wildcardType.getUpperBounds(), this::transformType, Type[]::new),
                NncUtils.mapArray(wildcardType.getLowerBounds(), this::transformType, Type[]::new)
        );
    }

    public Type transformGenericArrayType(GenericArrayType genericArrayType) {
        return new GenericArrayTypeImpl(transformType(genericArrayType.getGenericComponentType()));
    }

}
