package tech.metavm.util;

import java.util.Collection;

public class ValueUtil {

    public static boolean isBothInteger(Object a, Object b) {
        return isInteger(a) && isInteger(b);
    }

    public static boolean isNumber(Object value) {
        return isInteger(value) || isFloat(value);
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
