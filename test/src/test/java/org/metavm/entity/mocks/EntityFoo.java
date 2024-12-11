package org.metavm.entity.mocks;

import org.metavm.api.Entity;

@Entity
public class EntityFoo extends org.metavm.entity.Entity {

    public String name;

    public EntityFoo(String name) {
        this.name = name;
    }
}
