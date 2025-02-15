package org.metavm.entity;

import org.jetbrains.annotations.NotNull;
import org.metavm.object.instance.core.Id;

import java.util.Arrays;
import java.util.Objects;

public record Subtree(
        Id id,
        Id parentId,
        byte[] data,
        int entityTag
) implements Comparable<Subtree> {

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Subtree subTree)) return false;
        return id == subTree.id && Objects.equals(parentId, subTree.parentId) && Arrays.equals(data, subTree.data);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, parentId);
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }

    @Override
    public int compareTo(@NotNull Subtree o) {
        return id.compareTo(o.id);
    }
}
