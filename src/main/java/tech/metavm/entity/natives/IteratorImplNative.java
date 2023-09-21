package tech.metavm.entity.natives;

import tech.metavm.entity.IInstanceContext;
import tech.metavm.object.instance.ArrayInstance;
import tech.metavm.object.instance.BooleanInstance;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.NncUtils;

public class IteratorImplNative extends NativeBase {

    private final ClassInstance instance;
    private ArrayInstance array;
    private int size;
    private int index;

    public IteratorImplNative(ClassInstance instance, IInstanceContext context) {
        super(context);
        this.instance = instance;
    }

    public void init(ClassInstance collection) {
        var listArrayField = collection.getType().getFieldByCodeRequired("array");
        array = (ArrayInstance) collection.get(listArrayField);
        size = array.size();
    }

    public BooleanInstance hasNext() {
        return InstanceUtils.booleanInstance(index < size);
    }

    public Instance next() {
        NncUtils.requireTrue(index < size);
        return array.get(index++);
    }

}
