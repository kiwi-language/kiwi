package org.metavm.mocks;

import org.metavm.api.Entity;

import java.util.Objects;

@Entity(since = 1)
public class UpgradeBar extends org.metavm.entity.Entity {

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
