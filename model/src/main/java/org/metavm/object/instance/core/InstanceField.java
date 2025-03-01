package org.metavm.object.instance.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.metavm.common.ErrorCode;
import org.metavm.object.type.Field;
import org.metavm.object.type.Type;
import org.metavm.util.BusinessException;
import org.metavm.util.Instances;
import org.metavm.util.InternalException;
import org.metavm.util.MvOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class InstanceField implements IInstanceField {

    private static final Logger logger = LoggerFactory.getLogger(InstanceField.class);

    private final ClassInstance owner;
    private final Field field;
    private final Type type;
    public @Nullable Value value;

    InstanceField(ClassInstance owner, Field field, Type type) {
        this.owner = owner;
        this.field = field;
        this.type = type;
    }

    InstanceField(ClassInstance owner, Field field, Type type, @NotNull Value value) {
        this(owner, field, type);
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
        return field.isTransient() || value == null || value.shouldSkipWrite();
    }

    @Override
    public void writeValue(MvOutput output) {
        Objects.requireNonNull(value, () -> "Field " + field.getQualifiedName() + " is not initialized");
        if (value instanceof EntityReference r && r.isResolved() && r.get().isChildOf(owner))
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
        checkValue(value);
        this.value = value;
    }

    void secretlySet(Value value) {
        this.value = value;
    }

    void ensureInitialized() {
        if(value == null)
            value = Instances.getDefaultValue(type);
    }

    @Override
    public void clear() {
        this.value = null;
    }

    void checkValue(Value value) {
        if(field.isMetadataRemoved() && value.isNull())
            return;
        if (!type.isInstance(value)) {
            throw new BusinessException(ErrorCode.INCORRECT_INSTANCE_FIELD_VALUE,
                    value + " " + value.getValueType(), field.getQualifiedName(), field.getType());
        }
    }

    @Override
    public @NotNull Value getValue() {
        return Objects.requireNonNull(value,
                () -> "Field " + field.getQualifiedName() + " is not initialized");
    }

    @SuppressWarnings("unused")
    public ClassInstance getOwner() {
        return owner;
    }

    public String getDisplayValue() {
        if (type.isArray()) {
            return "";
        }
        return field.getDisplayValue(value);
    }

    @SuppressWarnings("unused")
    public boolean isPrimitive() {
        return type.isPrimitive();
    }

    public boolean isArray() {
        return type.isArray();
    }

    @Override
    public boolean isFieldInitialized() {
        return value != null;
    }

    public ArrayInstance getInstanceArray() {
        assert value != null;
        return (ArrayInstance) ((Reference) value).get();
    }

    @Override
    public String toString() {
        return field.getName();
    }

    public Type getType() {
        return type;
    }
}
