package org.metavm.mocks;

import org.metavm.api.EntityField;
import org.metavm.api.EntityType;
import org.metavm.entity.Entity;

@EntityType
public class UpgradeFoo extends Entity {

    private final String name;
    @EntityField(since = 1)
    private final Object bar;

    public UpgradeFoo(String name, Object bar) {
        this.name = name;
        this.bar = bar;
    }

    public String getName() {
        return name;
    }

    public Object getBar() {
        return bar;
    }

    @Override
    protected String toString0() {
        return "name: " + name + ", bar: " + bar;
    }
}