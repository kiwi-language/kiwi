package org.metavm.mocks;

import org.metavm.api.EntityType;
import org.metavm.entity.Entity;

import java.util.Objects;

@EntityType(since = 1)
public class UpgradeBar extends Entity {

    private final String name;

    public UpgradeBar(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof UpgradeBar that && Objects.equals(that.getName(), name);
    }
}
