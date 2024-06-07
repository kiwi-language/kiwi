package tech.metavm.entity.mocks;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityType;

@EntityType
public class EntityFoo extends Entity {

    public String name;

    public EntityFoo(String name) {
        this.name = name;
    }
}
