package tech.metavm.entity.mocks;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

@EntityType("Foo")
public class EntityFoo extends Entity {

    @EntityField("名称")
    public String name;

    public EntityFoo(String name) {
        this.name = name;
    }
}
