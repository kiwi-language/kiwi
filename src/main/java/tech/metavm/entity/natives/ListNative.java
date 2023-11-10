package tech.metavm.entity.natives;

import tech.metavm.object.instance.core.*;
import tech.metavm.object.meta.ArrayType;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.StandardTypes;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.NncUtils;

public class ListNative extends NativeBase {

    private final ClassInstance instance;
    private final Field arrayField;
    private ArrayInstance array;

    public ListNative(ClassInstance instance) {
        this.instance = instance;
        arrayField = NncUtils.requireNonNull(instance.getType().findFieldByCode("array"));
        if(instance.isFieldInitialized(arrayField)) {
            array = (ArrayInstance) instance.getField(arrayField);
        }
    }

    public Instance List() {
        array = new ArrayInstance((ArrayType) arrayField.getType());
        instance.initField(arrayField, array);
        return instance;
    }

    public ClassInstance iterator() {
        var iteratorImplType = (ClassType) instance.getType().getDependency(StandardTypes.getIteratorImplType());
        var it = new ClassInstance(iteratorImplType);
        var itNative = (IteratorImplNative) NativeInvoker.getNativeObject(it);
        itNative.IteratorImpl(instance);
        return it;
    }

    public Instance get(Instance index) {
        return array.get(getInt(index));
    }

    public Instance set(Instance index, Instance value) {
        return array.set(getInt(index), value);
    }

    public BooleanInstance remove(Instance instance) {
        return InstanceUtils.booleanInstance(array.remove(instance));
    }

    public Instance removeAt(Instance index) {
        return array.remove(getInt(index));
    }

    public Instance contains(Instance value) {
        return InstanceUtils.booleanInstance(array.contains(value));
    }

    public void clear() {
        array.clear();
    }

    public void add(Instance instance) {
        array.add(instance);
    }

    public Instance isEmpty() {
        return InstanceUtils.booleanInstance(array.isEmpty());
    }

    public LongInstance size() {
        return InstanceUtils.longInstance(array.size());
    }


}
