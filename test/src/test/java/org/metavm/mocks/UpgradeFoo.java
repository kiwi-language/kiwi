package org.metavm.mocks;

import org.metavm.api.EntityField;
import org.metavm.api.EntityType;
import org.metavm.entity.Entity;

@EntityType
public class UpgradeFoo extends Entity {

    private final String name;
    @EntityField(since = 1)
    private final UpgradeBar bar;

    public UpgradeFoo(String name, UpgradeBar bar) {
        this.name = name;
        this.bar = bar;
    }

    public String getName() {
        return name;
    }

    public UpgradeBar getBar() {
        return bar;
    }
}