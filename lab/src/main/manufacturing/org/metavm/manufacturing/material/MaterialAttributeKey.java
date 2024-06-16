package org.metavm.manufacturing.material;

import org.metavm.api.EntityField;
import org.metavm.api.EntityType;

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
