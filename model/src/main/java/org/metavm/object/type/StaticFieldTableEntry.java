package org.metavm.object.type;

import org.metavm.api.EntityType;
import org.metavm.entity.Entity;
import org.metavm.object.instance.core.Value;

@EntityType
public class StaticFieldTableEntry extends Entity {
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
}
