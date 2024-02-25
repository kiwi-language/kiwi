package tech.metavm.manufacturing.material;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

@EntityType("物料属性键")
public class MaterialAttributeKey {

    @EntityField(value = "名称", asTitle = true)
    private final String name;

    public MaterialAttributeKey(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
