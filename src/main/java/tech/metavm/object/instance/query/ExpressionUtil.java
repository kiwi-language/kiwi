package tech.metavm.object.instance.query;

import tech.metavm.util.BusinessException;
import tech.metavm.util.ValueUtil;

import java.util.Collection;

public class ExpressionUtil {

    public static boolean isAllInteger(Object first, Object second) {
        return ValueUtil.isInteger(first) && ValueUtil.isInteger(second);
    }

    public static boolean isAllNumeric(Object first, Object second) {
        return ValueUtil.isNumber(first) && ValueUtil.isNumber(second);
    }

    public static Long castInteger(Object value) {
        if(ValueUtil.isInteger(value)) {
            return ((Number) value).longValue();
        }
        else {
            throw BusinessException.invalidExpressionValue("整数", value);
        }
    }

    public static Double castFloat(Object value) {
        if(ValueUtil.isNumber(value)) {
            return ((Number) value).doubleValue();
        }
        else {
            throw BusinessException.invalidExpressionValue("数值", value);
        }
    }

    public static Boolean castBoolean(Object value) {
        if(ValueUtil.isBoolean(value)) {
            return (Boolean) value;
        }
        else {
            throw BusinessException.invalidExpressionValue("布尔", value);
        }
    }

    public static String castString(Object value) {
        if(ValueUtil.isString(value)) {
            return (String) value;
        }
        else {
            throw BusinessException.invalidExpressionValue("文本", value);
        }
    }

    public static Collection<Object> castCollection(Object value) {
        if(ValueUtil.isCollection(value)) {
            return (Collection<Object>) value;
        }
        else {
            throw BusinessException.invalidExpressionValue("集合", value);
        }
    }

}
