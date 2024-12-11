package org.metavm.mocks;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityField;
import org.metavm.api.Value;

@Value(since = 1)
public record RecordToClassFoo<V>(@EntityField(since = 1) String name, V value) implements Comparable<RecordToClassFoo<V>> {

    @Value(since = 1)
    record InnerFoo() {}

    @Override
    public int compareTo(@NotNull RecordToClassFoo o) {
        return name.compareTo(o.name);
    }


}