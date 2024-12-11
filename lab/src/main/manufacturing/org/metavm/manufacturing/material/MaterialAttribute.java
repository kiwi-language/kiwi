package org.metavm.manufacturing.material;

import org.metavm.api.EntityField;
import org.metavm.api.Entity;

@Entity
public class MaterialAttribute {
    private final MaterialAttributeKey key;
    @EntityField(asTitle = true)
    private final String name;
    private Object value;

    public MaterialAttribute(MaterialAttributeKey key, Object value) {
        this.name = key.getName();
        this.key = key;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public MaterialAttributeKey getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
