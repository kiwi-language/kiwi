package tech.metavm.object.meta;

import tech.metavm.entity.IInstanceContext;
import tech.metavm.object.instance.IInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.InstanceArray;
import tech.metavm.object.instance.rest.InstanceArrayDTO;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.instance.rest.InstanceFieldDTO;
import tech.metavm.object.instance.rest.ReferenceDTO;
import tech.metavm.util.BusinessException;
import tech.metavm.util.EncodingUtils;
import tech.metavm.util.InternalException;
import tech.metavm.util.ValueUtil;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ValueFormatter {

    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public static final DateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static Object parse(Object rawValue, Type type, IInstanceContext context) {
        if(isNull(rawValue)) {
            if(type.isNullable()) {
                return null;
            }
            else {
                throw new InternalException("Type " + type.getName() + " can not have null value");
            }
        }
        if(type.isNullable()) {
            type = type.getUnderlyingType();
        }
        if(rawValue instanceof InstanceArrayDTO arrayDTO) {
            return parseArray(arrayDTO, context);
        }
        else {
            return parseOne(rawValue, type, context);
        }
    }

    public static InstanceArray parseArray(InstanceArrayDTO arrayDTO, IInstanceContext context) {
        List<Object> elements = new ArrayList<>();
        for (Object value : arrayDTO.elements()) {
            elements.add(parseOne(value, StandardTypes.OBJECT, context));
        }
        InstanceArray array;
        if(arrayDTO.id() !=  null) {
            array = (InstanceArray) context.get(arrayDTO.id());
            array.setElements(elements);
        }
        else {
            array = new InstanceArray(StandardTypes.ARRAY, elements);
            context.bind(array);
        }
        return array;
    }

    public static Instance parseInstance(InstanceDTO instanceDTO, IInstanceContext context) {
        Type actualType = context.getType(instanceDTO.typeId());
        Map<Field, Object> fieldValueMap = new HashMap<>();
        for (InstanceFieldDTO instanceFieldDTO : instanceDTO.fields()) {
            Field field = actualType.getField(instanceFieldDTO.fieldId());
            fieldValueMap.put(
                    field,
                    parseOne(instanceFieldDTO.value(), field.getType(), context)
            );
        }
        Instance instance;
        if(instanceDTO.id() != null) {
            instance = context.get(instanceDTO.id());
            fieldValueMap.forEach(instance::set);
        }
        else {
            instance = new Instance(fieldValueMap, actualType);
            context.bind(instance);
        }
        return instance;
    }

    public static Instance parseReference(ReferenceDTO referenceDTO, IInstanceContext context) {
        return context.get(referenceDTO.id());
    }

    private static Object parseOne(Object rawValue, Type type, IInstanceContext context) {
        type = type.getConcreteType();
        if(type.isInt()) {
            if(ValueUtil.isNumber(rawValue)) {
                return ((Number) rawValue).intValue();
            }
        }
        if(rawValue instanceof ReferenceDTO ref) {
            return parseReference(ref, context);
        }
        if(rawValue instanceof InstanceDTO instanceDTO) {
            return parseInstance(instanceDTO, context);
        }
        if(type.isLong() || type.isDate() || type.isTime()) {
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
        if(type.isObject()) {
            if(ValueUtil.isInteger(rawValue)) {
                return ((Number) rawValue).longValue();
            }
            if(ValueUtil.isFloat(rawValue)) {
                return ((Number) rawValue).doubleValue();
            }
            return rawValue;
        }
        throw invalidValue(rawValue, type);
    }

    public static Object format(Object value, Type type) {
        if(value == null) {
            return null;
        }
        if(type.isNullable()) {
            type = type.getUnderlyingType();
        }
        if(type.isPassword()) {
            return null;
        }
        if(value instanceof IInstance instance) {
            if(instance.getId() == null) {
                return instance.toDTO();
            }
            else {
                return new ReferenceDTO(instance.getId());
            }
        }
        return value;
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
