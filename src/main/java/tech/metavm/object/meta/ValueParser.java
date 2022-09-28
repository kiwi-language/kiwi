package tech.metavm.object.meta;

import tech.metavm.object.meta.rest.dto.FieldDTO;
import tech.metavm.util.BusinessException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ValueUtil;

import java.util.Collection;
import java.util.regex.Pattern;

public class ValueParser {

    public static Object convertDefaultValue(FieldDTO fieldDTO) {
        return new ValueParser(
                TypeCategory.getByCodeRequired(fieldDTO.type()),
                fieldDTO.multiValued(),
                fieldDTO.defaultValue()
        ).convert();
    }

    public static Object convertValue(TypeCategory type, boolean multiValued, Object value) {
        return new ValueParser(type, multiValued, value).convert();
    }

    private final TypeCategory fieldType;
    private final boolean multiValued;
    private final Object rawValue;

    public static final Pattern ID_PATTERN = Pattern.compile("[0-9]+");

    public static final Pattern ID_LIST_PATTERN = Pattern.compile("[0-9]+(\\s*,\\s*[0-9]+)*");

    public ValueParser(TypeCategory fieldType, boolean multiValued, Object rawValue) {
        this.fieldType = fieldType;
        this.multiValued = multiValued;
        this.rawValue = rawValue;
    }

    private Object convert() {
        if(isNull()) {
            return null;
        }
        if(multiValued) {
            Collection<?> collection = (Collection<?>) rawValue;
            return NncUtils.map(collection, this::convertOne);
        }
        else {
            return convertOne(rawValue);
        }
    }

    private Object convertOne(Object value) {
        if(fieldType.isInt32()) {
            if(ValueUtil.isNumber(value)) {
                return ((Number) value).intValue();
            }
        }
        if(fieldType.isInt64() || fieldType.isTime() || fieldType.isComposite()) {
            if(ValueUtil.isNumber(value)) {
                return ((Number) value).longValue();
            }
        }
        if(fieldType.isNumber()) {
            if(ValueUtil.isNumber(value) || ValueUtil.isFloat(value)) {
                return ((Number) value).doubleValue();
            }
        }
        if(fieldType.isBool()) {
            if(ValueUtil.isBoolean(value)) {
                return value;
            }
        }
        if(fieldType.isString()) {
            if(ValueUtil.isString(value)) {
                return value;
            }
        }
        throw invalidDefaultValue();
    }

    private boolean isNull() {
        return rawValue == null
                || (rawValue instanceof String && ((String) rawValue).length() == 0);
    }

    private BusinessException invalidDefaultValue() {
        return BusinessException.invalidDefaultValue(rawValue);
    }

}
