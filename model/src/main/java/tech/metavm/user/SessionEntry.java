package tech.metavm.user;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityType;
import tech.metavm.object.instance.core.Instance;

@EntityType("会话条目")
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
