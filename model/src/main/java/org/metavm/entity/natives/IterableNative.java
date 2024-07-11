package org.metavm.entity.natives;

import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.InstanceReference;

public abstract class IterableNative extends NativeBase {

    public abstract void forEach(Instance action, CallContext callContext);

    public abstract InstanceReference iterator(CallContext callContext);

}
