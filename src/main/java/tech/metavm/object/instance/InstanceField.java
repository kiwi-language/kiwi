package tech.metavm.object.instance;

import tech.metavm.object.instance.rest.InstanceFieldDTO;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.ValueFormatter;
import tech.metavm.util.*;

import static tech.metavm.util.PersistenceUtil.writeValue;

public class InstanceField {

    private final Instance owner;
    private final Field field;
    private Object value;

    InstanceField(Instance owner, Field field, Object value) {
        this.field = field;
        this.owner = owner;
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
        if(field.getColumn() == null) {
            throw new InternalException("Field " + field + " doesn't have a column");
        }
        return field.getColumn().name();
    }

    public Object getValue() {
        return value;
    }

//    public void set(InstanceFieldDTO instanceFieldDTO) {
//        Object rawValue = instanceFieldDTO.value();
//        if(NncUtils.isEmptyValue(rawValue) && field.isNotNull()) {
//            throw BusinessException.fieldValueRequired(field);
//        }
//        setValue(field.preprocessValue(rawValue));
//    }

    void setValue(Object value) {
        if(field.isNotNull() && value == null) {
            throw new InternalException("Field " + field + " can not be null");
        }
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

    public Object getColumnValue(long tenantId, IdentitySet<Instance> visited) {
        Type fieldType = field.getType();
        if(fieldType.isReference()) {
            return NncUtils.get((Instance) value, Instance::toReferencePO);
        }
        if(fieldType.isValue() || fieldType.isNullable() && fieldType.getUnderlyingType().isValue()) {
            return NncUtils.get((Instance) value, instance -> instance.toPO(tenantId, visited));
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

    public Instance getInstance() {
        return (Instance) value;
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
        return !field.getType().isPrimitive();
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
