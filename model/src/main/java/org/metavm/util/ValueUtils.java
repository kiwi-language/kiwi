package org.metavm.util;

import org.metavm.api.ValueObject;
import org.metavm.entity.*;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeCategory;
import org.metavm.object.type.Types;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class ValueUtils {

    public static final Pattern INT_PATTERN = Pattern.compile("-?[0-9]+");

    public static boolean isConvertible(Type from, Type to) {
        if (to.isString()) return true;
        if (to.isLong()) {
            return true;
        }
        return from.isLong() && to.isDouble();
    }

    private static final Set<Class<?>> PRIMITIVE_TYPES = Set.of(
            int.class, Integer.class, Long.class, long.class, float.class, Float.class,
            double.class, Double.class, String.class, boolean.class, Boolean.class,
            Password.class, Date.class
    );

    public static boolean isBoolean(Class<?> klass) {
        return klass == boolean.class || klass == Boolean.class;
    }

    public static boolean isInteger(Class<?> klass) {
        return klass == int.class || klass == Integer.class || klass == short.class || klass == Short.class
                || klass == byte.class || klass == Byte.class;
    }

    public static boolean isIntegerStr(String str) {
        return INT_PATTERN.matcher(str).matches();
    }

    public static boolean isLong(Class<?> klass) {
        return klass == long.class || klass == Long.class;
    }

    public static boolean isShort(Class<?> klass) {
        return klass == short.class || klass == Short.class;
    }

    public static boolean isByte(Class<?> klass) {
        return klass == byte.class || klass == Byte.class;
    }

    public static boolean isDouble(Class<?> klass) {
        return klass == double.class || klass == Double.class;
    }

    public static boolean isString(Class<?> klass) {
        return klass == String.class;
    }

    public static boolean isPassword(Class<?> klass) {
        return klass == Password.class;
    }

    public static boolean isTime(Class<?> klass) {
        return klass == Date.class;
    }

    public static boolean isPrimitive(Object object) {
        return isPrimitiveType(object.getClass());
    }

    public static boolean isJavaType(Object object) {
        return java.lang.reflect.Type.class.isAssignableFrom(object.getClass());
    }

    public static boolean isEnumConstant(Object object) {
        return isEnumType(object.getClass());
    }

    public static boolean isPrimitiveType(Class<?> klass) {
        return PRIMITIVE_TYPES.contains(klass);
    }

    public static boolean isArrayType(Class<?> klass) {
        return ReadonlyArray.class.isAssignableFrom(klass);
    }

    public static boolean isChildArrayType(Class<?> klass) {
        return ChildArray.class.isAssignableFrom(klass);
    }

    public static boolean isEntityType(Class<?> klass) {
        return Entity.class.isAssignableFrom(klass);
    }

    public static boolean isValueType(Class<?> klass) {
        return ValueObject.class.isAssignableFrom(klass);
    }

    public static boolean isEnumType(Class<?> klass) {
        return Enum.class.isAssignableFrom(klass);
    }

    public static TypeCategory getTypeCategory(Class<?> klass) {
        return getTypeCategory((java.lang.reflect.Type) klass);
    }

    public static TypeCategory getTypeCategory(java.lang.reflect.Type type) {
        if (type instanceof Class<?> klass) {
            if (BiUnion.class.isAssignableFrom(klass))
                return TypeCategory.UNION;
            if (klass.isInterface())
                return TypeCategory.INTERFACE;
            if(isInteger(klass))
                return TypeCategory.INT;
            if (isLong(klass))
                return TypeCategory.LONG;
            if (isDouble(klass))
                return TypeCategory.DOUBLE;
            if (isTime(klass))
                return TypeCategory.TIME;
            if (isBoolean(klass))
                return TypeCategory.BOOLEAN;
            if (isString(klass))
                return TypeCategory.STRING;
            if (isPassword(klass))
                return TypeCategory.PASSWORD;
            if (Date.class.equals(klass))
                return TypeCategory.TIME;
            if (Password.class.equals(klass))
                return TypeCategory.PASSWORD;
            if (Null.class.equals(klass))
                return TypeCategory.NULL;
            if (Object.class.equals(klass) || Record.class.isAssignableFrom(klass) || isValueType(klass))
                return TypeCategory.VALUE;
            if (isArrayType(klass)) {
                if (isChildArrayType(klass))
                    return TypeCategory.CHILD_ARRAY;
                else if (ReadWriteArray.class.isAssignableFrom(klass))
                    return TypeCategory.READ_WRITE_ARRAY;
                else if(ValueArray.class.isAssignableFrom(klass))
                    return TypeCategory.VALUE_ARRAY;
                else if(ReadonlyArray.class.isAssignableFrom(klass))
                    return TypeCategory.READ_ONLY_ARRAY;
                else
                    throw new InternalException("Unrecognized array class: " + klass.getName());
            }
            if (isEnumType(klass))
                return TypeCategory.ENUM;
            if (isEntityType(klass))
                return TypeCategory.CLASS;
            if (Class.class == klass)
                return TypeCategory.CLASS;
        }
        if (type instanceof ParameterizedType parameterizedType) {
            if (parameterizedType.getRawType() instanceof Class<?> rawClass) {
                if (ChildArray.class.isAssignableFrom(rawClass))
                    return TypeCategory.CHILD_ARRAY;
                else if (ReadWriteArray.class.isAssignableFrom(rawClass))
                    return TypeCategory.READ_WRITE_ARRAY;
                else if(ValueArray.class.isAssignableFrom(rawClass))
                    return TypeCategory.VALUE_ARRAY;
                else if (ReadonlyArray.class.isAssignableFrom(rawClass))
                    return TypeCategory.READ_ONLY_ARRAY;
                else
                    return getTypeCategory(rawClass);
            }
        }
        return TypeCategory.CLASS;
    }

    public static boolean isAssignable(Type from, Type to) {
        if (to.isAssignableFrom(from)) {
            return true;
        }
        if (from.isPrimitive() && to.isPrimitive()) {
            if (Types.isDouble(to)) {
                return Types.isLong(from);
            }
        }
        return false;
    }

    public static boolean isBothInteger(Object a, Object b) {
        return isInteger(a) && isInteger(b);
    }

    public static boolean isNumber(Object value) {
        return isInteger(value) || isFloatOrDouble(value);
    }

    public static boolean isLong(Object value) {
        return value instanceof Long;
    }

    public static boolean isInteger(Object value) {
        return value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long;
    }

    public static boolean isString(Object value) {
        return value instanceof String;
    }

    public static boolean isFloatOrDouble(Object value) {
        return value instanceof Float || value instanceof Double;
    }

    public static boolean isDouble(Object value) {
        return value instanceof Double;
    }

    public static boolean isFloat(Object value) {
        return value instanceof Float;
    }

    public static boolean isCollection(Object value) {
        return value instanceof Collection;
    }

    public static boolean isLongList(Object value) {
        if (value instanceof List<?> list) {
            for (Object item : list) {
                if (!(item instanceof Long)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static boolean isIntegerColl(Object value) {
        if (value instanceof Collection) {
            for (Object o : ((Collection) value)) {
                if (!isInteger(o)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public static long getLong(Object columnValue) {
        if (!isInteger(columnValue)) {
            throw new InternalException("Value '" + columnValue + "' can not be converted to a long value");
        }
        return ((Number) columnValue).longValue();
    }
}
