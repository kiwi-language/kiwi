package tech.metavm.entity.natives;

import tech.metavm.object.instance.core.ArrayInstance;
import tech.metavm.object.instance.core.BooleanInstance;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.NncUtils;

public class IteratorImplNative extends NativeBase {

    private final ClassInstance instance;
    private ArrayInstance array;
    private int size;
    private int index;

    public IteratorImplNative(ClassInstance instance) {
        this.instance = instance;
    }

    public Instance IteratorImpl(ClassInstance collection) {
        var listArrayField = collection.getType().getFieldByCode("array");
        array = (ArrayInstance) collection.getField(listArrayField);
        size = array.size();
        return instance;
    }

    public BooleanInstance hasNext() {
        return InstanceUtils.booleanInstance(index < size);
    }

    public Instance next() {
        NncUtils.requireTrue(index < size);
        return array.get(index++);
    }

}
