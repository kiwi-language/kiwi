package org.metavm.entity;

import org.jetbrains.annotations.NotNull;
import org.metavm.object.instance.core.Id;

import javax.annotation.Nullable;
import java.util.Objects;

public record DiffId(Id id, @Nullable Id oldId, boolean useOldId) implements Comparable<DiffId> {

    @Override
    public Id id() {
        return id;
    }

    @Override
    @Nullable
    public Id oldId() {
        return oldId;
    }

    @Override
    public boolean useOldId() {
        return useOldId;
    }

    public Id getId() {
        return useOldId ? Objects.requireNonNull(oldId) : id;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DiffId that && this.id.equals(that.id);
    }

    @Override
    public int compareTo(@NotNull DiffId o) {
        return id.compareTo(o.id);
    }
}
