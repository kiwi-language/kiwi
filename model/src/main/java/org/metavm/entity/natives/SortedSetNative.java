package org.metavm.entity.natives;

import org.metavm.object.instance.core.Value;

public interface SortedSetNative extends SequencedCollectionNative {

    Value first(CallContext callContext);

}
