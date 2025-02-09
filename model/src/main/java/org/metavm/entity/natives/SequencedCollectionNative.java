package org.metavm.entity.natives;

import org.metavm.object.instance.core.Value;

public interface SequencedCollectionNative extends IterableNative {
    Value getFirst(CallContext callContext);

    Value getLast(CallContext callContext);
}
