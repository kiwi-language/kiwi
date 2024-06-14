package org.metavm.entity.natives;

import org.metavm.object.instance.core.ArrayInstance;
import org.metavm.object.instance.core.BooleanInstance;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Instance;
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

    public Instance IteratorImpl(ClassInstance collection, CallContext callContext) {
        var arrayField = collection.getKlass().getFieldByCode("array");
        array = (ArrayInstance) collection.getField(arrayField);
        size = array.size();
        return instance;
    }

    public BooleanInstance hasNext(CallContext callContext) {
        return hasNext();
    }

    public BooleanInstance hasNext() {
        return Instances.booleanInstance(index < size);
    }

    public Instance next(CallContext callContext) {
        NncUtils.requireTrue(index < size);
        return array.get(index++);
    }

}
