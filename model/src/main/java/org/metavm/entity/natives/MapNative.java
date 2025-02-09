package org.metavm.entity.natives;

import org.metavm.object.instance.core.Value;

public interface MapNative extends NativeBase {

    Value keySet(CallContext callContext);

    Value get(Value key, CallContext callContext);

    Value getOrDefault(Value key, Value value, CallContext callContext);

    Value put(Value key, Value value, CallContext callContext);

    Value containsKey(Value key, CallContext callContext);

    Value remove(Value key, CallContext callContext);

    Value size(CallContext callContext);

    Value clear(CallContext callContext);

    Value hashCode(CallContext callContext);

    Value equals(Value o, CallContext callContext);


}
