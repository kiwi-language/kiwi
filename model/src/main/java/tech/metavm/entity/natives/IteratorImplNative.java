package tech.metavm.entity.natives;

import tech.metavm.object.instance.core.ArrayInstance;
import tech.metavm.object.instance.core.BooleanInstance;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.util.Instances;
import tech.metavm.util.NncUtils;

public class IteratorImplNative extends NativeBase {

    private final ClassInstance instance;
    private ArrayInstance array;
    private int size;
    private int index;

    public IteratorImplNative(ClassInstance instance) {
        this.instance = instance;
    }

    public Instance IteratorImpl(ClassInstance collection, CallContext callContext) {
        var arrayField = collection.getType().getFieldByCode("array");
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
