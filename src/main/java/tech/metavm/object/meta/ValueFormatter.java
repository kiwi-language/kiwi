package tech.metavm.object.meta;

import tech.metavm.util.BusinessException;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ValueUtil;

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
            return NncUtils.map((Collection<?>) rawValue, item -> parse(item, type.getBaseType()));
        }
        else {
            return parseOne(rawValue, type);
        }
    }

    private static Object parseOne(Object rawValue, Type type) {
        if(type.isNullable()) {
            type = type.getBaseType();
        }
        TypeCategory typeCategory = type.getCategory();
        if(typeCategory.isInt32()) {
            if(ValueUtil.isNumber(rawValue)) {
                return ((Number) rawValue).intValue();
            }
        }
//        if(typeCategory.isDate() || typeCategory.isTime()) {
//            return parseDate((String) rawValue);
//        }
        if(typeCategory.isInt64() || typeCategory.isComposite()
                || typeCategory.isDate() || typeCategory.isTime()) {
            if(ValueUtil.isNumber(rawValue)) {
                return ((Number) rawValue).longValue();
            }
        }
        if(typeCategory.isNumber()) {
            if(ValueUtil.isNumber(rawValue) || ValueUtil.isFloat(rawValue)) {
                return ((Number) rawValue).doubleValue();
            }
        }
        if(typeCategory.isBool()) {
            if(ValueUtil.isBoolean(rawValue)) {
                return rawValue;
            }
        }
        if(typeCategory.isString()) {
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
            return NncUtils.map((Collection<?>) value, item -> format(item, type.getBaseType()));
        }
        else {
            return formatOne(value, type);
        }
    }

    private static Object formatOne(Object value, Type type) {
        if(type.isNullable()) {
            type = type.getBaseType();
        }
//        if(type.isDate() || type.isTime()) {
//            return formatTime((long) value);
//        }
//        else {
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
