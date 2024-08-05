package org.metavm.object.instance.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.metavm.common.ErrorCode;
import org.metavm.object.instance.rest.InstanceFieldDTO;
import org.metavm.object.type.Field;
import org.metavm.util.BusinessException;
import org.metavm.util.InstanceOutput;
import org.metavm.util.InternalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class InstanceField implements IInstanceField {

    private static final Logger logger = LoggerFactory.getLogger(InstanceField.class);

    private final ClassInstance owner;
    private final Field field;
    private @Nullable Value value;

    InstanceField(ClassInstance owner, Field field) {
        this.owner = owner;
        this.field = field;
    }

    InstanceField(ClassInstance owner, Field field, @NotNull Value value) {
        this(owner, field);
        this.value = value;
    }

    public Field getField() {
        return field;
    }

    public long getKlassTag() {
        return field.getKlassTag();
    }

    public int getTag() {
        return field.getTag();
    }

    @Override
    public boolean shouldSkipWrite() {
        return value == null || value.shouldSkipWrite();
    }

    @Override
    public void writeValue(InstanceOutput output) {
        Objects.requireNonNull(value, () -> "Field " + field.getQualifiedName() + " is not initialized");
        if (value instanceof Reference r && r.isResolved() && r.resolve().isChildOf(owner, field))
            output.writeInstance(value);
        else
            output.writeValue(value);
    }

    public String getName() {
        return field.getName();
    }

    String getColumnName() {
        if (field.getColumn() == null)
            throw new InternalException("Field " + field + " doesn't have a column");
        return field.getColumn().name();
    }

    public void set(Value value) {
        value = checkValue(value);
        if (field.isChild() && value.isNotNull())
            ((Reference) value).resolve().setParent(this.owner, this.field);
        this.value = value;
    }

    @Override
    public void clear() {
        this.value = null;
    }

    Value checkValue(Value value) {
        if (field.getType().isInstance(value)) {
            return value;
        }
        else if(field.isMetadataRemoved() && value.isNull())
            return value;
        else {
            try {
                return value.convert(field.getType());
            } catch (BusinessException e) {
                throw new BusinessException(ErrorCode.INCORRECT_INSTANCE_FIELD_VALUE,
                        field.getQualifiedName(), e.getMessage());
            }
        }
    }

    @Override
    public @NotNull Value getValue() {
        return Objects.requireNonNull(value, "Field " + field.getQualifiedName() + " is not initialized");
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
                field.getTagId().toString(),
                field.getName(),
                field.getType().getConcreteType().getCategory().code(),
                field.getType().isArray(),
                getValue().toFieldValueDTO()
        );
    }

    @Override
    public boolean isFieldInitialized() {
        return value != null;
    }

    public ArrayInstance getInstanceArray() {
        assert value != null;
        return (ArrayInstance) ((Reference) value).resolve();
    }

    @Override
    public String toString() {
        return field.getName();
    }
}
