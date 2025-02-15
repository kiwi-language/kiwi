package org.metavm.entity;

import org.jetbrains.annotations.NotNull;
import org.metavm.object.instance.core.Id;

public record DiffId(Id id, int entityTag) implements Comparable<DiffId> {

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DiffId that && this.id.equals(that.id);
    }

    @Override
    public int compareTo(@NotNull DiffId o) {
        return id.compareTo(o.id);
    }
}
