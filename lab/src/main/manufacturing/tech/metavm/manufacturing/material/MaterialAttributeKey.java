package tech.metavm.manufacturing.material;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

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
