package org.metavm.manufacturing.material;

import org.metavm.entity.EntityField;
import org.metavm.entity.EntityType;

@EntityType
public class MaterialAttributeKey {

    @EntityField(asTitle = true)
    private final String name;

    public MaterialAttributeKey(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
