package org.metavm.entity.natives;

import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Instance;

public abstract class IterableNative extends NativeBase {

    public abstract void forEach(Instance action, CallContext callContext);

    public abstract ClassInstance iterator(CallContext callContext);

}
