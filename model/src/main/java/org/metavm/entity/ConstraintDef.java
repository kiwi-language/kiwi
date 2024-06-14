package org.metavm.entity;

import org.metavm.util.TypeReference;

import java.lang.reflect.Type;

public record ConstraintDef<T>(Class<T> type, Type genericType, String expression) {

    public static <T> ConstraintDef<T> create(Class<T> type, String expression) {
        return new ConstraintDef<>(type, type, expression);
    }

    public static <T> ConstraintDef<T> create(TypeReference<T> typeRef, String expression) {
        return new ConstraintDef<>(typeRef.getType(), typeRef.getGenericType(), expression);
    }


}
