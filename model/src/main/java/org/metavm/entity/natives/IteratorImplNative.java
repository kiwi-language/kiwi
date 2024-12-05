package org.metavm.entity.natives;

import org.metavm.object.instance.core.ArrayInstance;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Value;
import org.metavm.util.Instances;
import org.metavm.util.NncUtils;

public class IteratorImplNative extends NativeBase {

    private final ClassInstance instance;
    private ArrayInstance array;
    private int size;
    private int index;

    public IteratorImplNative(ClassInstance instance) {
        this.instance = instance;
    }

    public Value IteratorImpl(ClassInstance collection, CallContext callContext) {
        var arrayField = collection.getKlass().getFieldByName("array");
        array = collection.getField(arrayField).resolveArray();
        size = array.size();
        return instance.getReference();
    }

    public Value hasNext(CallContext callContext) {
        return hasNext();
    }

    public Value hasNext() {
        return Instances.intInstance(index < size);
    }

    public Value next(CallContext callContext) {
        NncUtils.requireTrue(index < size);
        return array.get(index++);
    }

}
