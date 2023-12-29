package tech.metavm.entity.natives;

import tech.metavm.common.ErrorCode;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.type.ArrayType;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Field;
import tech.metavm.entity.StandardTypes;
import tech.metavm.object.type.rest.dto.InstanceParentRef;
import tech.metavm.util.BusinessException;
import tech.metavm.util.Instances;
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
        array = new ArrayInstance((ArrayType) arrayField.getType(),
                new InstanceParentRef(instance, arrayField));
        return instance;
    }

    public Instance List(Instance c) {
        if(c instanceof ClassInstance collection) {
            var thatArrayField = collection.getType().getFieldByCode("array");
            var thatArray = (ArrayInstance) collection.getField(thatArrayField);
            array = new ArrayInstance((ArrayType) arrayField.getType(),
                    new InstanceParentRef(instance, arrayField));
            array.addAll(thatArray);
            return instance;
        }
        else
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
    }

    public ClassInstance iterator() {
        var iteratorImplType = (ClassType) instance.getType().getDependency(StandardTypes.getIteratorImplType());
        var it = ClassInstance.allocate(iteratorImplType);
        var itNative = (IteratorImplNative) NativeMethods.getNativeObject(it);
        itNative.IteratorImpl(instance);
        return it;
    }

    public Instance get(Instance index) {
        return array.get(getInt(index));
    }

    public Instance set(Instance index, Instance value) {
        return array.setElement(getInt(index), value);
    }

    public BooleanInstance remove(Instance instance) {
        return Instances.booleanInstance(array.removeElement(instance));
    }

    public Instance removeAt(Instance index) {
        return array.removeElement(getInt(index));
    }

    public Instance contains(Instance value) {
        return Instances.booleanInstance(array.contains(value));
    }

    public void clear() {
        array.clear();
    }

    public void add(Instance instance) {
        array.addElement(instance);
    }

    public Instance isEmpty() {
        return Instances.booleanInstance(array.isEmpty());
    }

    public LongInstance size() {
        return Instances.longInstance(array.size());
    }


}
