package tech.metavm.object.instance;

import tech.metavm.object.instance.rest.InstanceFieldDTO;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.ValueFormatter;
import tech.metavm.util.BusinessException;
import tech.metavm.util.Column;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ValueUtil;

public class InstanceField {

//    private final InstanceContext context;
    private final Instance owner;
    private final Field field;
    private Object value;

    public InstanceField(Instance owner, Field field, InstanceFieldDTO instanceFieldDTO) {
        this.field = field;
        this.owner = owner;
//        this.context = owner.getContext();
        set(instanceFieldDTO);
    }

    InstanceField(Instance owner, Field field, Object value) {
        this.field = field;
        this.owner = owner;
//        this.context = owner.getContext();
        setValue(value);
    }

    public Field getField() {
        return field;
    }

    public long getId() {
        return field.getId();
    }

    public String getName() {
        return field.getName();
    }

    @SuppressWarnings("unused")
    Column getColumn() {
        return field.getColumn();
    }

    String getColumnName() {
        return field.getColumn().name();
    }

    public Object getValue() {
        return value;
    }

//    public Object getResolvedValue() {
//        if(field.isTable() || field.isEnum()) {
//            return NncUtils.get(getValueId(), context::get);
//        }
//        else {
//            return value;
//        }
//    }

    public void set(InstanceFieldDTO instanceFieldDTO) {
        Object rawValue = instanceFieldDTO.value();
        if(NncUtils.isEmptyValue(rawValue) && field.isNotNull()) {
            throw BusinessException.fieldValueRequired(field);
        }
        setValue(field.preprocessValue(rawValue));
    }

    void setValue(Object value) {
        this.value = value;
    }

    @SuppressWarnings("unused")
    private Object check(Object value) {
        if(value == null) {
            if(field.isNotNull()) {
                throw BusinessException.fieldRequired(field);
            }
            return null;
        }
        if(field.isInt64() || field.isTime() || field.isCustomTyped()) {
            if(ValueUtil.isInteger(value)) {
                return ((Number) value).longValue();
            }
        }
        if(field.isNumber()) {
            if(ValueUtil.isNumber(value)) {
                return ((Number) value).doubleValue();
            }
        }
        if(field.isString()) {
            if(ValueUtil.isString(value)) {
                return value;
            }
        }
        if(field.isBool()) {
            if(ValueUtil.isBoolean(value)) {
                return value;
            }
        }
        throw BusinessException.invalidFieldValue(field, value);
    }

    public Object getColumnValue() {
        if(isGeneralRelation()) {
            throw new UnsupportedOperationException(
                    "getColumnValue is not supported for general relation fields"
            );
        }
        return value;
    }

    public Long getLong() {
        return (Long) value;
    }

    public String getString() {
        return (String) value;
    }

    public Boolean getBoolean() {
        return (Boolean) value;
    }

    public Double getDouble() {
        return (Double) value;
    }

//    public Long getValueId() {
//        return (Long) value;
//    }

    public IInstance getInstance() {
        return (IInstance) value;
//        if(!isGeneralRelation() || isArray()) {
//            throw new UnsupportedOperationException();
//        }
//        if(value == null) {
//            return null;
//        }
//        return context.get((long) value);
    }

    @SuppressWarnings("unused")
    public Instance getOwner() {
        return owner;
    }

    public String getDisplayValue() {
        if(field.isArray()) {
            return "";
        }
        return field.getDisplayValue(value);
    }

    @SuppressWarnings("unused")
    public boolean isPrimitive() {
        return field.isPrimitive();
    }

    public boolean isGeneralPrimitive() {
        return field.isGeneralPrimitive();
    }

    public boolean isGeneralRelation() {
        return !field.getConcreteType().isPrimitive();
    }

    public boolean isArray() {
        return field.isArray();
    }

    public InstanceFieldDTO toDTO () {
        return new InstanceFieldDTO(
                field.getId(),
                field.getName(),
                field.getConcreteTypeCategory().code(),
                field.isArray(),
                ValueFormatter.format(value, field.getType()),
                getDisplayValue()
        );
    }

    public InstanceField copy(Instance ownerCopy) {
        return new InstanceField(ownerCopy, field, value);
    }

    public InstanceArray getInstanceArray() {
        return (InstanceArray) value;
    }

    @Override
    public String toString() {
        return field.getName();
    }
}
