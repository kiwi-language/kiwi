package org.metavm.entity.mocks;

import org.metavm.entity.Entity;
import org.metavm.entity.EntityType;

@EntityType
public class EntityFoo extends Entity {

    public String name;

    public EntityFoo(String name) {
        this.name = name;
    }
}
