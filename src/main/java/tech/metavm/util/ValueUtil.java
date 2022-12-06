package tech.metavm.util;

import tech.metavm.entity.Entity;
import tech.metavm.entity.ValueType;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.StandardTypes;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.TypeCategory;

import java.lang.reflect.ParameterizedType;
import java.util.*;

public class ValueUtil {

    public static Type getValueType(Object value) {
        if(value instanceof String) {
            return StandardTypes.STRING;
        }
        if(isFloat(value)) {
            return StandardTypes.DOUBLE;
        }
        if(value instanceof Integer) {
            return StandardTypes.INT;
        }
        if(value instanceof Long) {
            return StandardTypes.LONG;
        }
        if(isBoolean(value)) {
            return StandardTypes.BOOL;
        }
        if(isTime(value)) {
            return StandardTypes.TIME;
        }
        if(value instanceof Instance instance) {
            return instance.getType();
        }
        throw new InternalException("Unsupported value: " + value);
    }

    public static Type getConvertibleType(Type type1, Type type2) {
        if(type1.equals(type2)) {
            return type1;
        }
        if(isAssignable(type1, type2)) {
            return type2;
        }
        if(isAssignable(type2, type1)) {
            return type1;
        }
        throw new InternalException("category " + type1 + " and category " + type2 + " are incompatible");
    }

    public static Type getConvertibleType(List<Type> types) {
        NncUtils.requireMinimumSize(types, 1);
        Iterator<Type> it = types.iterator();
        Type compatibleType = it.next();
        while (it.hasNext()) {
            Type t = it.next();
            if (!t.equals(compatibleType) && !isAssignable(t, compatibleType)) {
                if(isAssignable(compatibleType, t)) {
                    compatibleType = t;
                }
                else {
                    throw new InternalException("Types are not compatible: " + types);
                }
            }
        }
        return compatibleType;
    }

    private static final Set<Class<?>> PRIMITIVE_TYPES = Set.of(
        int.class, Integer.class, Long.class, long.class, float.class, Float.class,
            double.class, Double.class, String.class, boolean.class, Boolean.class
    );

    public static boolean isBoolean(Class<?> klass) {
        return klass == boolean.class || klass == Boolean.class;
    }

    public static boolean isInteger(Class<?> klass) {
        return klass == int.class || klass == Integer.class || klass == short.class || klass == Short.class
                || klass == byte.class || klass == Byte.class;
    }

    public static boolean isLong(Class<?> klass) {
        return klass == long.class || klass == Long.class;
    }

    public static boolean isString(Class<?> klass) {
        return klass == String.class;
    }

    public static boolean isTime(Class<?> klass) {
        return klass == Date.class;
    }

    public static boolean isFloat(Class<?> klass) {
        return klass == Double.class || klass == double.class || klass == Float.class || klass == float.class;
    }

    public static boolean isPrimitiveType(Class<?> klass) {
        return PRIMITIVE_TYPES.contains(klass);
    }

    public static boolean isArrayType(Class<?> klass) {
        return klass == Table.class;
    }

    public static boolean isEntityType(Class<?> klass) {
        return Entity.class.isAssignableFrom(klass);
    }

    public static boolean isValueType(Class<?> klass) {
        return klass.isAnnotationPresent(ValueType.class);
    }

    public static boolean isEnumType(Class<?> klass) {
        return Enum.class.isAssignableFrom(klass);
    }

    public static Type getPrimitiveType(Class<?> klass) {
        if(isBoolean(klass)) {
            return StandardTypes.BOOL;
        }
        if(isString(klass)) {
            return StandardTypes.STRING;
        }
        if(isInteger(klass)) {
            return StandardTypes.INT;
        }
        if (isLong(klass)) {
            return StandardTypes.LONG;
        }
        if(isTime(klass)) {
            return StandardTypes.TIME;
        }
        if(isFloat(klass)) {
            return StandardTypes.DOUBLE;
        }
        throw new InternalException("Type " + klass.getName() + " is not a primitive type");
    }

    public static TypeCategory getTypeCategory(Class<?> klass) {
        return getTypeCategory((java.lang.reflect.Type) klass);
    }

    public static TypeCategory getTypeCategory(java.lang.reflect.Type type) {
        if(type instanceof Class<?> klass) {
            if (isPrimitiveType(klass)) {
                return TypeCategory.PRIMITIVE;
            }
            if (isArrayType(klass)) {
                return TypeCategory.ARRAY;
            }
            if (isEnumType(klass)) {
                return TypeCategory.ENUM;
            }
            if(Object.class.equals(klass) || Record.class.equals(klass) || isValueType(klass)) {
                return TypeCategory.VALUE;
            }
            if(Date.class.equals(klass)) {
                return TypeCategory.TIME;
            }
            if(Password.class.equals(klass)) {
                return TypeCategory.PASSWORD;
            }
            if(Null.class.equals(klass)) {
                return TypeCategory.NULL;
            }
            if (isEntityType(klass)) {
                return TypeCategory.CLASS;
            }
        }
        if(type instanceof ParameterizedType parameterizedType) {
            if(parameterizedType.getRawType() instanceof Class<?> rawClass) {
                if(Collection.class.isAssignableFrom(rawClass)) {
                    return TypeCategory.ARRAY;
                }
                else if(Map.class.isAssignableFrom(rawClass)) {

                }
                else {
                    return getTypeCategory(rawClass);
                }
            }
        }
        throw new InternalException("Invalid valid type: " + type);
    }

    public static Type getCommonSuperType(Collection<Type> types) {
        NncUtils.requireMinimumSize(types, 1);
//        EntityContext context = types.get(0).getContext();
        Iterator<Type> it = types.iterator();
        Type commonSuperType = it.next();
        while (it.hasNext()) {
            Type t = it.next();
            while (!commonSuperType.isAssignableFrom(t)) {
                commonSuperType = commonSuperType.getSuperType();
                if(commonSuperType == null) {
                    throw new InternalException("Can not find common super type for types: " + types);
                }
            }
        }
        return commonSuperType;
    }

    public static boolean isAssignable(Type from, Type to) {
//        EntityContext context = from.getContext();
        if(to.isAssignableFrom(from)) {
            return true;
        }
        if(from.isPrimitive() && to.isPrimitive()) {
            if(to.isDouble()) {
                return from.isInt() || from.isLong();
            }
            if(to.isLong()) {
                return from.isInt();
            }
        }
        return false;
    }

    public static boolean isBothInteger(Object a, Object b) {
        return isInteger(a) && isInteger(b);
    }

    public static boolean isNumber(Object value) {
        return isInteger(value) || isFloat(value);
    }

    public static boolean isTime(Object value) {
        return value instanceof Date;
    }

    public static boolean isInteger(Object value) {
        return value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long;
    }

    public static boolean isString(Object value) {
        return value instanceof String;
    }

    public static boolean isBoolean(Object value) {
        return value instanceof Boolean;
    }

    public static boolean isFloat(Object value) {
        return value instanceof Float || value instanceof Double;
    }

    public static boolean isCollection(Object value) {
        return value instanceof Collection;
    }

    public static boolean isLongList(Object value) {
        if(value instanceof List<?> list) {
            for (Object item : list) {
                if(!(item instanceof Long)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static boolean isIntegerColl(Object value) {
        if(value instanceof Collection) {
            for (Object o : ((Collection) value)) {
                if(!isInteger(o)) {
                    return false;
                }
            }
            return true;
        }
        else {
            return false;
        }
    }
}
