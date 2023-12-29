package tech.metavm.object.instance.core;

import org.jetbrains.annotations.NotNull;
import tech.metavm.object.instance.rest.InstanceFieldDTO;
import tech.metavm.object.type.Field;
import tech.metavm.util.InternalException;

public class InstanceField {

    private final ClassInstance owner;
    private final Field field;
    private @NotNull Instance value;

    InstanceField(ClassInstance owner, Field field, Instance value) {
        this(owner, field, value, true);
    }

    InstanceField(ClassInstance owner, Field field, Instance value, boolean check) {
        this.field = field;
        this.owner = owner;
        if(field.isChild() && value.isNotNull())
            ((DurableInstance) value).initParent(this.owner, this.field);
        if (value instanceof DurableInstance d)
            new ReferenceRT(owner, d, field);
        this.value = check ? checkValue(value) : value;
    }

    public Field getField() {
        return field;
    }

    public long getId() {
        return field.getIdRequired();
    }

    public String getName() {
        return field.getName();
    }

    String getColumnName() {
        if (field.getColumn() == null)
            throw new InternalException("Field " + field + " doesn't have a column");
        return field.getColumn().name();
    }

    void setValue(Instance value) {
        value = checkValue(value);
        if(field.isChild() && value.isNotNull())
            ((DurableInstance) value).initParent(this.owner, this.field);
        if (this.value.isNotPrimitive())
            owner.getOutgoingReference(this.value, field).clear();
        if (value instanceof DurableInstance d)
            new ReferenceRT(owner, d, field);
        this.value = value;
    }

    Instance checkValue(Instance value) {
        if (field.getType().isInstance(value)) {
            return value;
        } else {
            if(value.isNull() && !field.isReady())
                return value;
            else
                return value.convert(field.getType());
        }
    }

    public @NotNull Instance getValue() {
        return value;
    }

    @SuppressWarnings("unused")
    public ClassInstance getOwner() {
        return owner;
    }

    public String getDisplayValue() {
        if (field.getType().isArray()) {
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

    public InstanceFieldDTO toDTO() {
        return new InstanceFieldDTO(
                field.getId(),
                field.getName(),
                field.getType().getConcreteType().getCategory().code(),
                field.getType().isArray(),
                value.toFieldValueDTO()
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
