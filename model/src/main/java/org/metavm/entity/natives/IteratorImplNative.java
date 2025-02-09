package org.metavm.entity.natives;

import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Value;
import org.metavm.util.Instances;

import java.util.Iterator;

public class IteratorImplNative implements NativeBase {

    private final ClassInstance instance;
    private Iterator<Value> iterator;

    public IteratorImplNative(ClassInstance instance) {
        this.instance = instance;
    }

    public Value IteratorImpl(ClassInstance collection, CallContext callContext) {
        var nat = (IterableNative) NativeMethods.getNativeObject(collection);
        iterator = nat.iterator();
        return instance.getReference();
    }

    public Value hasNext(CallContext callContext) {
        return hasNext();
    }

    public Value hasNext() {
        return Instances.intInstance(iterator.hasNext());
    }

    public Value next(CallContext callContext) {
        return iterator.next();
    }

}
