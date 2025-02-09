package org.metavm.entity.natives;

import org.metavm.object.instance.core.Value;

public interface CollectionNative extends IterableNative {
    Value add(Value value, CallContext callContext);

    Value contains(Value value, CallContext callContext);

    Value size(CallContext callContext);

    Value clear(CallContext callContext);

    Value isEmpty(CallContext callContext);

    Value remove(Value value, CallContext callContext);

    Value containsAll(Value values, CallContext callContext);

    Value retainAll(Value values, CallContext callContext);
}
