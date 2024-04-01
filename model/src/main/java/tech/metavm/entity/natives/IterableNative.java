package tech.metavm.entity.natives;

import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;

public abstract class IterableNative extends NativeBase {

    public abstract void forEach(Instance action, CallContext callContext);

    public abstract ClassInstance iterator(CallContext callContext);

}
