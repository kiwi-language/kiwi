package tech.metavm.object.instance.query;

import tech.metavm.util.ValueUtil;

public class FunctionMethods {

    private FunctionMethods() {}

    public static Number MAX(Number a, Number b) {
        if(ValueUtil.isBothInteger(a, b)) {
            return Math.max(a.longValue(), b.longValue());
        }
        else {
            return Math.max(a.doubleValue(), b.doubleValue());
        }
    }

    public static Number MIN(Number a, Number b) {
        if(ValueUtil.isBothInteger(a, b)) {
            return Math.max(a.longValue(), b.longValue());
        }
        else {
            return Math.max(a.doubleValue(), b.doubleValue());
        }
    }

    public static Number SUM(Number a, Number b) {
        if(ValueUtil.isBothInteger(a, b)) {
            return a.longValue() + b.longValue();
        }
        else {
            return a.doubleValue() + b.doubleValue();
        }
    }

    public static boolean IS_BLANK(Object value) {
        if(value == null) {
            return true;
        }
        if(value instanceof String str) {
            return str.length() == 0;
        }
        return false;
    }
}
