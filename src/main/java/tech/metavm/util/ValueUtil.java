package tech.metavm.util;

import com.fasterxml.jackson.core.sym.NameN;
import tech.metavm.entity.EntityContext;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.BuiltinTypes;
import tech.metavm.object.meta.Type;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class ValueUtil {

    public static Type getValueType(Object value) {
        if(value instanceof String) {
            return BuiltinTypes.getString();
        }
        if(isFloat(value)) {
            return BuiltinTypes.getDouble();
        }
        if(isInteger(value)) {
            return BuiltinTypes.getInt64();
        }
        if(isBoolean(value)) {
            return BuiltinTypes.getBool();
        }
        if(isTime(value)) {
            return BuiltinTypes.getTime();
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
        if(isConvertible(type1, type2)) {
            return type2;
        }
        if(isConvertible(type2, type1)) {
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
            if (!t.equals(compatibleType) && !isConvertible(t, compatibleType)) {
                if(isConvertible(compatibleType, t)) {
                    compatibleType = t;
                }
                else {
                    throw new InternalException("Types are not compatible: " + types);
                }
            }
        }
        return compatibleType;
    }


    public static Type getCompatible(List<Type> types) {
        NncUtils.requireMinimumSize(types, 1);
        Iterator<Type> it = types.iterator();
        Type compatibleType = it.next();
        while (it.hasNext()) {
            Type t = it.next();
            if (!t.equals(compatibleType)) {
                return BuiltinTypes.getObject();
            }
        }
        return compatibleType;
    }

    public static boolean isConvertible(Type from, Type to) {
        if(from.isPrimitive() && to.isPrimitive()) {
            if(to.equals(BuiltinTypes.getDouble())) {
                return from.equals(BuiltinTypes.getInt32()) || from.equals(BuiltinTypes.getInt64());
            }
            if(to.equals(BuiltinTypes.getInt64())) {
                return from.equals(BuiltinTypes.getInt32());
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
