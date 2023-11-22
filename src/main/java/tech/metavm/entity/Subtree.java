package tech.metavm.entity;

import org.jetbrains.annotations.NotNull;
import tech.metavm.object.instance.persistence.InstancePO;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

public record Subtree(
        long id,
        long parentId,
        long parentFieldId,
        byte[] data
) implements Comparable<Subtree> {
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Subtree subTree)) return false;
        return id == subTree.id && parentId == subTree.parentId && parentFieldId == subTree.parentFieldId && Arrays.equals(data, subTree.data);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, parentId, parentFieldId);
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }

    @Override
    public int compareTo(@NotNull Subtree o) {
        return Long.compare(id, o.id);
    }
}
