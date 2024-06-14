package org.metavm.object.instance;

import org.jetbrains.annotations.NotNull;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.util.Instances;

import java.util.Objects;

public class IndexEntryRT implements Comparable<IndexEntryRT> {

    private final IndexKeyRT key;
    private final ClassInstance instance;

    public IndexEntryRT(IndexKeyRT key, ClassInstance instance) {
        this.key = key;
        this.instance = instance;
    }

    public IndexKeyRT getKey() {
        return key;
    }

    public ClassInstance getInstance() {
        return instance;
    }

    @Override
    public int compareTo(@NotNull IndexEntryRT o) {
        var keyComparison = key.compareTo(o.key);
        if (keyComparison != 0)
            return keyComparison;
        return Instances.compare(instance, o.instance);
    }

    @Override
    public boolean equals(Object entity) {
        if (this == entity) return true;
        if (!(entity instanceof IndexEntryRT that)) return false;
        return Objects.equals(key, that.key) && Objects.equals(instance, that.instance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, instance);
    }
}
