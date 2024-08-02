package org.metavm.user;

import org.metavm.api.EntityType;
import org.metavm.entity.Entity;
import org.metavm.object.instance.core.Value;

@EntityType
public class SessionEntry extends Entity  {
    private final String key;
    private Value value;

    public SessionEntry(String key, Value value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }
}
