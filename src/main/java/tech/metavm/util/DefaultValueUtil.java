package tech.metavm.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import tech.metavm.object.meta.TypeCategory;

import java.util.List;
import java.util.regex.Pattern;

public class DefaultValueUtil {

    public static final Pattern INTEGER_PATTERN = Pattern.compile("-?[0-9]+");

    public static final Pattern NUMBER_PATTERN = Pattern.compile("-?[0-9]+(\\.[0-9]+)?");

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().enable(JsonGenerator.Feature.IGNORE_UNKNOWN);

    public static Object convertFromStr(String str, TypeCategory typeCategory) {
        if(isNull(str)) {
            return null;
        }
        if(typeCategory.isArray()) {
            List<Object> values = NncUtils.readJSONString(str, new TypeReference<>() {});
            return NncUtils.map(values, value -> convertFromStrOne(NncUtils.toString(value), typeCategory));
        }
        else {
            return convertFromStrOne(str, typeCategory);
        }
    }

    public static Object convertFromStrOne(String str, TypeCategory fieldType) {
        return switch (fieldType) {
            case INT32, INT64, TABLE, TIME, DATE, ENUM, PAGE, INTERFACE, FLOW_INPUT, NULLABLE -> parseLong(str);
            case DOUBLE -> parseDouble(str);
            case BOOL -> parseBool(str);
            case OBJECT, ARRAY -> null;
            case STRING -> str;
        };
    }

    private static boolean isNull(String str) {
        return str == null || str.length() == 0;
    }

    public static boolean isNotNull(String str) {
        return !isNull(str);
    }

    public static String convertToStr(Object value, int fieldTypeCode, boolean multiValued) {
        if(value == null) {
            return "";
        }
        if(multiValued) {
            return NncUtils.toJSONString(value);
        }
        else {
            return value.toString();
        }
    }

    public static boolean isInteger(String str) {
        return INTEGER_PATTERN.matcher(str).matches();
    }

    public static boolean isNumber(String str) {
        return NUMBER_PATTERN.matcher(str).matches();
    }

    public static boolean isStringColl(String str) {
        try {
            OBJECT_MAPPER.readValue(str, new TypeReference<List<String>>() {});
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    public static boolean isIntegerColl(String str) {
        try {
            OBJECT_MAPPER.readValue(str, new TypeReference<List<Long>>() {});
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    public static boolean isBool(Object value) {
        return value instanceof Boolean;
    }

    public static Long parseLong(String str) {
        return Long.valueOf(str);
    }

    public static Double parseDouble(String str) {
        return Double.parseDouble(str);
    }

    public static Boolean parseBool(String str) {
        return Boolean.parseBoolean(str);
    }

    public static List<String> parseStringColl(String str) {
        try {
            return OBJECT_MAPPER.readValue(str, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON deserialization failed, json string: " + str, e);
        }
    }

    public static List<Long> parseIntegerColl(String str) {
        try {
            return OBJECT_MAPPER.readValue(str, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON deserialization failed, json string: " + str, e);
        }
    }

    public static String toJSONString(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON serialization failed, value: " + value, e);
        }
    }

}
