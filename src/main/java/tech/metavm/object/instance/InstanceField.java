package tech.metavm.object.instance;

import tech.metavm.object.instance.rest.InstanceFieldDTO;
import tech.metavm.object.instance.rest.ValueDTO;
import tech.metavm.object.meta.ChoiceOption;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.ValueFormatter;
import tech.metavm.util.BusinessException;
import tech.metavm.util.Column;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ValueUtil;

import java.util.List;

public class InstanceField {

    private final InstanceContext context;
    private final Instance owner;
    private final Field field;
    private Object value;

    public InstanceField(Instance owner, Field field, InstanceFieldDTO instanceFieldDTO) {
        this.field = field;
        this.owner = owner;
        this.context = owner.getContext();
        setRawValue(instanceFieldDTO.value());
    }

    public InstanceField(Instance owner, Field field, Object value) {
        this.field = field;
        this.owner = owner;
        this.context = owner.getContext();
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

    Column getColumn() {
        return field.getColumn();
    }

    String getColumnName() {
        return field.getColumn().name();
    }

    public Object getValue() {
        return value;
    }

    public Object getResolvedValue() {
        if(field.isTable() || field.isEnum()) {
            if(field.isArray()) {
                return NncUtils.map(getValueIds(), context::get);
            }
            else {
                return NncUtils.get(getValueId(), context::get);
            }
        }
        else {
            return value;
        }
    }

    public void setRawValue(Object rawValue) {
        this.value = field.preprocessValue(rawValue);
    }

    public void setValue(Object value) {
        this.value = value;
    }

    private Object check(Object value) {
        if(value == null) {
            if(field.isNotNull()) {
                throw BusinessException.fieldRequired(field);
            }
            return null;
        }
        if(field.isInt64() || field.isTime() || field.isComposite()) {
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

    public List<Long> getValueIds() {
        return (List<Long>) value;
    }

    public Long getValueId() {
        return (Long) value;
    }

    public Instance getInstance() {
        if(!isGeneralRelation() || isArray()) {
            throw new UnsupportedOperationException();
        }
        if(value == null) {
            return null;
        }
        return context.get((long) value);
    }

    public Instance getOwner() {
        return owner;
    }

    public List<Long> getDestInstanceIds() {
        if(isPrimitive()) {
            throw new UnsupportedOperationException("getDestInstanceIds is only supported for relation fields");
        }
        return getIdList();
    }

    private List<Long> getIdList() {
        if(!isGeneralRelation()) {
            throw new UnsupportedOperationException();
        }
        if(value == null) {
            return List.of();
        }
        if(isArray()) {
            return (List) value;
        }
        else {
            return List.of((Long) value);
        }
    }

    public String getDisplayValue() {
        if(field.isArray()) {
            return "";
        }
        return getDisplayValue(value);
    }

    public String getDisplayValue(Object value) {
        if(value == null) {
            return "";
        }
        if(field.getConcreteType().isEnum()) {
            return NncUtils.filterOneAndMap(
                    field.getChoiceOptions(),
                    opt -> opt.getId().equals(value),
                    ChoiceOption::getName);
        }
        else if(field.getConcreteType().isTable()) {
            return context.getTitle((Long) value);
        }
        else if(field.getConcreteType().isBool()) {
            if(Boolean.TRUE.equals(value)) {
                return "是";
            }
            else if(Boolean.FALSE.equals(value)) {
                return "否";
            }
            else {
                return "";
            }
        }
        else if(field.getConcreteType().isTime()) {
            return ValueFormatter.formatTime((Long) value);
        }
        else if(field.getConcreteType().isDate()) {
            return ValueFormatter.formatDate((Long) value);
        }
        return NncUtils.toString(value);
    }

    public boolean isPrimitive() {
        return field.isPrimitive();
    }

    public boolean isGeneralRelation() {
        return field.isComposite();
    }

    public boolean isArray() {
        return field.isArray();
    }

    public InstanceFieldDTO toDTO () {
        List<ValueDTO> values = isArray() ?
            NncUtils.map(getValueIds(), destId -> new ValueDTO(destId, getDisplayValue(destId))) : null;
        return new InstanceFieldDTO(
                field.getId(),
                field.getName(),
                field.getConcreteTypeCategory().code(),
                field.isArray(),
                ValueFormatter.format(value, field.getType()),
                getDisplayValue(),
                values
        );
    }

    public InstanceField copy(Instance ownerCopy) {
        return new InstanceField(ownerCopy, field, value);
    }

}
