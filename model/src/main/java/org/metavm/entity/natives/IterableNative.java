package org.metavm.entity.natives;

import org.jetbrains.annotations.NotNull;
import org.metavm.object.instance.core.Value;

import java.util.Iterator;
import java.util.function.Consumer;

public abstract class IterableNative extends NativeBase implements Iterable<Value> {

    public abstract Value forEach(Value action, CallContext callContext);

    public abstract Value iterator(CallContext callContext);

    public abstract void forEach(Consumer<? super Value> action);

    public abstract @NotNull Iterator<Value> iterator();

}
