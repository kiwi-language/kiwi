package tech.metavm.object.meta;

import tech.metavm.object.instance.IInstance;
import tech.metavm.object.instance.IInstanceArray;
import tech.metavm.object.instance.InstanceArray;
import tech.metavm.util.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

public class ValueFormatter {

    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public static final DateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static Object parse(Object rawValue, Type type) {
        if(isNull(rawValue)) {
            return null;
        }
        if(type.isArray()) {
            return NncUtils.map((Collection<?>) rawValue, item -> parse(item, type.getElementType()));
        }
        else {
            return parseOne(rawValue, type);
        }
    }

    private static Object parseOne(Object rawValue, Type type) {
        type = type.getConcreteType();
        if(type.isInt()) {
            if(ValueUtil.isNumber(rawValue)) {
                return ((Number) rawValue).intValue();
            }
        }
        if(type.isLong() || type.isClass() || type.isEnum()
                || type.isDate() || type.isTime()) {
            if(ValueUtil.isNumber(rawValue)) {
                return ((Number) rawValue).longValue();
            }
        }
        if(type.isDouble()) {
            if(ValueUtil.isNumber(rawValue) || ValueUtil.isFloat(rawValue)) {
                return ((Number) rawValue).doubleValue();
            }
        }
        if(type.isBool()) {
            if(ValueUtil.isBoolean(rawValue)) {
                return rawValue;
            }
        }
        if(type.isPassword()) {
            return EncodingUtils.md5((String) rawValue);
        }
        if(type.isString()) {
            if(ValueUtil.isString(rawValue)) {
                return rawValue;
            }
        }
        throw invalidValue(rawValue, type);
    }

    public static Object format(Object value, Type type) {
        if(value == null) {
            return null;
        }
        if(type.isArray()) {
            IInstanceArray instanceArray = (IInstanceArray) value;
            return NncUtils.map(instanceArray.getElements(), item -> format(item, type.getElementType()));
        }
        else {
            return formatOne(value, type);
        }
    }

    private static Object formatOne(Object value, Type type) {
//        if(category.isNullable()) {
//            category = category.getBaseType();
//        }
//        if(category.isDate() || category.isTime()) {
//            return formatTime((long) value);
//        }
//        else {
        if(type.isPassword()) {
            return null;
        }
        if(value instanceof IInstance instance) {
            if(instance.getType().getCategory().isValue()) {
                return instance.toDTO();
            }
            else {
                return instance.getId();
            }
        }
        return value;
//        }
    }

    public static String formatDate(Long time) {
        if(time == null) {
            return null;
        }
        return DATE_FORMAT.format(new Date(time));
    }

    public static String formatTime(Long time) {
        if(time == null) {
            return null;
        }
        return DATE_TIME_FORMAT.format(new Date(time));
    }

    public static Long parseDate(String source) {
        if(source == null) {
            return null;
        }
        try {
            return DATE_TIME_FORMAT.parse(source).getTime();
        } catch (ParseException e) {
            throw new InternalException(
                    "fail to parse date string '" + source + "' with date format: " + DATE_TIME_FORMAT, e
            );
        }
    }

    private static boolean isNull(Object rawValue) {
        return rawValue == null
                || (rawValue instanceof String && ((String) rawValue).length() == 0);
    }

    private static BusinessException invalidValue(Object rawValue, Type type) {
        return BusinessException.invalidValue(type, rawValue);
    }

}
