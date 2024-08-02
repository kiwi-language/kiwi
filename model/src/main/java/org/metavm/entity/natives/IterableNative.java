package org.metavm.entity.natives;

import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;

public abstract class IterableNative extends NativeBase {

    public abstract void forEach(Value action, CallContext callContext);

    public abstract Reference iterator(CallContext callContext);

}
