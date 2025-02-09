package org.metavm.entity.natives;

import org.metavm.object.instance.core.Value;

public interface CharSequenceNative extends NativeBase {
    Value isEmpty(CallContext callContext);

    Value length(CallContext callContext);

    Value charAt(Value index, CallContext callContext);

    Value toString(CallContext callContext);
}
