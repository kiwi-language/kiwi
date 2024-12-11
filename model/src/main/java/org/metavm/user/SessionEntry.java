package org.metavm.user;

import org.metavm.api.Entity;
import org.metavm.object.instance.core.Value;

@Entity
public class SessionEntry extends org.metavm.entity.Entity {
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
