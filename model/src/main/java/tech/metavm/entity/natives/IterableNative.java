package tech.metavm.entity.natives;

import tech.metavm.object.instance.core.Instance;

public abstract class IterableNative extends NativeBase {

    public abstract void forEach(Instance action, NativeCallContext callContext);

}
