package org.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.BuildKeyContext;
import org.metavm.entity.Entity;
import org.metavm.entity.LocalKey;
import org.metavm.object.instance.core.Value;

@EntityType
public class StaticFieldTableEntry extends Entity implements LocalKey {
    private final Field field;
    private Value value;

    public StaticFieldTableEntry(Field field, Value value) {
        this.field = field;
        this.value = value;
    }

    public Field getField() {
        return field;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    @Override
    public boolean isValidLocalKey() {
        return true;
    }

    @Override
    public String getLocalKey(@NotNull BuildKeyContext context) {
        return field.getName();
    }
}
