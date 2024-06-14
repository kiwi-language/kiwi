package org.metavm.util;


import java.lang.reflect.*;

public abstract class JavaTypeVisitor {

    public void visitType(Type type) {
        switch (type) {
            case Class<?> klass -> visitClass(klass);
            case ParameterizedType pType -> visitParameterizedType(pType);
            case GenericArrayType genericArrayType -> visitGenericArrayType(genericArrayType);
            case TypeVariable<?> typeVariable -> visitTypeVariable(typeVariable);
            case WildcardType wildcardType -> visitWildcardType(wildcardType);
            default -> throw new IllegalStateException("Unexpected value: " + type);
        }
    }

    public void visitClass(Class<?> klass) {
        if(klass.getGenericSuperclass() != null) {
            visitType(klass.getGenericSuperclass());
        }
        for (Type genericInterface : klass.getGenericInterfaces()) {
            visitType(genericInterface);
        }
    }

    public void visitParameterizedType(ParameterizedType pType) {
        if(pType.getOwnerType() != null) {
            visitType(pType.getOwnerType());
        }
        visitType(pType.getRawType());
        for (Type actualTypeArgument : pType.getActualTypeArguments()) {
            visitType(actualTypeArgument);
        }
    }

    public void visitGenericArrayType(GenericArrayType genericArrayType) {
        visitType(genericArrayType.getGenericComponentType());
    }

    public void visitTypeVariable(TypeVariable<?> typeVariable) {
    }

    public void visitWildcardType(WildcardType wildcardType) {
        for (Type upperBound : wildcardType.getUpperBounds()) {
            visitType(upperBound);
        }
        for (Type lowerBound : wildcardType.getLowerBounds()) {
            visitType(lowerBound);
        }
    }

}
