package org.metavm.entity.natives;

import org.jetbrains.annotations.NotNull;
import org.metavm.object.instance.core.Value;
import org.metavm.util.Instances;

public record ComparableKeyWrap(Value value, CallContext callContext) implements Comparable<ComparableKeyWrap> {

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ComparableKeyWrap that && Instances.equals(value, that.value, callContext);
    }

    @Override
    public int compareTo(@NotNull ComparableKeyWrap o) {
        return Instances.compare(value, o.value, callContext);
    }
}
