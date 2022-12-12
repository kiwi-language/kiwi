package tech.metavm.object.instance;

import tech.metavm.object.instance.rest.InstanceFieldDTO;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.ValueFormatter;
import tech.metavm.util.*;

public class InstanceField {

    private final ClassInstance owner;
    private final Field field;
    private Instance value;

    InstanceField(ClassInstance owner, Field field, Instance value) {
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

    String getColumnName() {
        if(field.getColumn() == null) {
            throw new InternalException("Field " + field + " doesn't have a column");
        }
        return field.getColumn().name();
    }

    void setValue(Instance value) {
        if(field.getType().isInstance(value)) {
            this.value = value;
        }
        else {
            throw new InternalException("Value '" + value + "' is not assignable to field '" + field + "'");
        }
    }

    public Object getColumnValue(long tenantId, IdentitySet<Instance> visited) {
        if(field.isReference()) {
            return NncUtils.requireNonNull(value.getId());
        }
        else {
            return value.toColumnValue(tenantId, visited);
        }
    }

    public Instance getValue() {
        return value;
    }

    @SuppressWarnings("unused")
    public ClassInstance getOwner() {
        return owner;
    }

    public String getDisplayValue() {
        if(field.getType().isArray()) {
            return "";
        }
        return field.getDisplayValue(value);
    }

    @SuppressWarnings("unused")
    public boolean isPrimitive() {
        return field.getType().isPrimitive();
    }

    public boolean isArray() {
        return field.getType().isArray();
    }

    public InstanceFieldDTO toDTO () {
        return new InstanceFieldDTO(
                field.getId(),
                field.getName(),
                field.getType().getConcreteType().getCategory().code(),
                field.getType().isArray(),
                ValueFormatter.format(value),
                getDisplayValue()
        );
    }

    public ArrayInstance getInstanceArray() {
        return (ArrayInstance) value;
    }

    @Override
    public String toString() {
        return field.getName();
    }
}
