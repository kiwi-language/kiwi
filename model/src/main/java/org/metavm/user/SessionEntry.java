package org.metavm.user;

import org.metavm.entity.Entity;
import org.metavm.api.EntityType;
import org.metavm.object.instance.core.Instance;

@EntityType
public class SessionEntry extends Entity  {
    private final String key;
    private Instance value;

    public SessionEntry(String key, Instance value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public Instance getValue() {
        return value;
    }

    public void setValue(Instance value) {
        this.value = value;
    }
}
