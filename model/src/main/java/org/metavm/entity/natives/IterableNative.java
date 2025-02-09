package org.metavm.entity.natives;

import org.jetbrains.annotations.NotNull;
import org.metavm.object.instance.core.Value;

import java.util.Iterator;
import java.util.function.Consumer;

public interface IterableNative extends NativeBase, Iterable<Value> {

    Value forEach(Value action, CallContext callContext);

    Value iterator(CallContext callContext);

    void forEach(Consumer<? super Value> action);

    @NotNull Iterator<Value> iterator();

}
