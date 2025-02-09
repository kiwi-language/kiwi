package org.metavm.entity;

import lombok.extern.slf4j.Slf4j;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.CheckConstraint;
import org.metavm.object.type.Index;
import org.metavm.object.type.Klass;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.lang.reflect.*;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class ModelIdentities {

    public static ModelIdentity getIdentity(Object o) {
        return new ModelIdentity(getModelType(o), getModelName(o), false);
    }

    public static String getModelName(Object o) {
        return getModelName(o, null);
    }

    private static String getModelName(Object o, @Nullable Object current) {
        if (current != null && current.equals(o)) return "this";
        return switch (o) {
            case Class<?> c -> {
                if (c == Object.class || c == Value.class)
                    yield "any";
                else if (c == Date.class)
                    yield "time";
                else if (c == Class.class)
                    yield "org.metavm.object.type.Klass";
                else if (c.getDeclaringClass() != null)
                    yield  getModelName(c.getDeclaringClass()) + "." + c.getSimpleName();
                else if (c.isArray())
                    yield getNullableTypeExpr(c.getComponentType(), current, true) + "[]";
                else
                    yield  c.getName();
            }
            case Method m -> {
                var className = getModelName(m.getDeclaringClass());
                var typeExpressions = Utils.join(
                        m.getGenericParameterTypes(),
                        t -> getNullableTypeExpr(t, m, false)
                );
                yield  className + "." + m.getName() + "(" + typeExpressions + ")";
            }
            case Constructor<?> c -> {
                var className = getModelName(c.getDeclaringClass());
                var typeExpressions = Utils.join(
                        c.getGenericParameterTypes(),
                        t -> getNullableTypeExpr(t, c, false)
                );
                yield  className + "." + c.getDeclaringClass().getSimpleName() + "(" + typeExpressions + ")";
            }
            case Field f -> getModelName(f.getDeclaringClass()) + "." + f.getName();
            case TypeVariable<?> tv -> getModelName(tv.getGenericDeclaration(), current) + "." + tv.getName();
            case Parameter p -> getModelName(p.getDeclaringExecutable()) + "." + p.getName();
            case ParameterizedType parameterizedType -> {
                if (parameterizedType.getRawType() == Class.class)
                    yield "org.metavm.object.type.Klass";
                else
                    yield getModelName(parameterizedType.getRawType()) + "<" +
                            Utils.join(List.of(parameterizedType.getActualTypeArguments()), t -> getTypeExpr(t, current))
                            + ">";
            }
            case GenericArrayType genericArrayType -> getNullableTypeExpr(genericArrayType.getGenericComponentType(), current, true) + "[]";
            case WildcardType wildcardType ->
                    "[" + getUnionTypeName(wildcardType.getLowerBounds(), current) + "," + getIntersectionTypeName(wildcardType.getUpperBounds(), current) + "]";
            default -> throw new IllegalArgumentException("Cannot get model identity for " + o.getClass().getName());
        };
    }

    private static String getIntersectionTypeName(Type[] types, @Nullable Object current) {
        if (types.length == 0) return "any";
        else if (types.length == 1) return getTypeExpr(types[0], current);
        else return Stream.of(types).map(t -> getTypeExpr(t, current)).sorted().collect(Collectors.joining("&"));
    }

    private static String getUnionTypeName(Type[] types, @Nullable Object current) {
        if (types.length == 0) return "never";
        else if (types.length == 1) return getTypeExpr(types[0], current);
        else return Stream.of(types).map(t -> getTypeExpr(t, current)).sorted().collect(Collectors.joining("|"));
    }

    private static String getTypeExpr(Type type, @Nullable Object current) {
        var expr = getModelName(type, current);
        if (type instanceof TypeVariable<?>) expr = "@" + expr;
        return expr;
    }

    private static String getNullableTypeExpr(Type type, @Nullable Object current, boolean parenthesized) {
        var expr = getTypeExpr(type, current);
        if (!(type instanceof Class<?> c && c.isPrimitive())) {
            if (parenthesized) {
                if (expr.compareTo("null") < 0) return "(" + expr + "|null)";
                else return "(null|" + expr + ")";
            } else {
                if (expr.compareTo("null") < 0) return expr + "|null";
                else return "null|" + expr ;

            }
        }
        else return expr;
    }

    private static Type getModelType(Object o) {
        return switch (o) {
            case Class<?> ignored -> Klass.class;
            case Method ignored -> org.metavm.flow.Method.class;
            case Constructor<?> ignored -> org.metavm.flow.Method.class;
            case Field field -> {
                if (Modifier.isStatic(field.getModifiers()) && field.getType() == IndexDef.class)
                    yield Index.class;
                if (Modifier.isStatic(field.getModifiers()) && field.getType() == ConstraintDef.class)
                    yield CheckConstraint.class;
                else
                    yield org.metavm.object.type.Field.class;
            }
            case TypeVariable<?> ignored -> org.metavm.object.type.TypeVariable.class;
            case Parameter ignored -> org.metavm.flow.Parameter.class;
            default -> throw new IllegalArgumentException("Cannot get model type for " + o);
        };
    }

}
