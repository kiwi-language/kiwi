package tech.metavm.manufacturing.material;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

@EntityType("物料属性")
public class MaterialAttribute {
    @EntityField("键")
    private final MaterialAttributeKey key;
    @EntityField(value = "名称" ,asTitle = true)
    private final String name;
    @EntityField("值")
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
