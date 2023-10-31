package tech.metavm.entity.natives;

import tech.metavm.entity.CollectionKind;
import tech.metavm.object.instance.ArrayInstance;
import tech.metavm.object.instance.ArrayType;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.NncUtils;

import java.util.HashMap;
import java.util.Map;

public class SetNative extends NativeBase {

    private final ClassInstance instance;
    private final Map<Instance, Integer> element2index = new HashMap<>();
    private final Field arrayField;
    private ArrayInstance array;

    public SetNative(ClassInstance instance) {
        this.instance = instance;
        arrayField = NncUtils.requireNonNull(instance.getType().getFieldByCode("array"));
        if(instance.isFieldInitialized(arrayField)) {
            array = (ArrayInstance) instance.getField(arrayField);
        }
    }

    public Instance Set() {
        array = new ArrayInstance((ArrayType) arrayField.getType());
        instance.initializeField(arrayField, array);
        return instance;
    }

    public Instance iterator() {
        var iteratorImplType = (ClassType) instance.getType().getDependency(CollectionKind.ITERATOR_IMPL);
        var it = ClassInstance.allocate(iteratorImplType);
        var itNative = (IteratorImplNative) NativeInvoker.getNativeObject(it);
        itNative.IteratorImpl(instance);
        return it;
    }

    public Instance add(Instance value) {
        if (!element2index.containsKey(value)) {
            element2index.put(value, array.size());
            array.add(value);
            return InstanceUtils.trueInstance();
        } else {
            return InstanceUtils.falseInstance();
        }
    }

    public Instance remove(Instance value) {
        Integer index = element2index.remove(value);
        if (index != null) {
            int lastIdx = array.size() - 1;
            var last = array.remove(lastIdx);
            if (index != lastIdx) {
                array.set(index, last);
                element2index.put(last, index);
            }
            return InstanceUtils.trueInstance();
        }
        else {
            return InstanceUtils.falseInstance();
        }
    }

    public Instance isEmpty() {
        return InstanceUtils.booleanInstance(element2index.isEmpty());
    }

    public Instance contains(Instance value) {
        return InstanceUtils.booleanInstance(element2index.containsKey(value));
    }

    public Instance size() {
        return InstanceUtils.longInstance(element2index.size());
    }

    public void clear() {
        array.clear();
        element2index.clear();
    }

}
