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
        array = new ArrayInstance((ArrayType) arrayField.getType());
        instance.initField(arrayField, array);
        return instance;
    }

    public Instance List(Instance c) {
        if(c instanceof ClassInstance collection) {
            var thatArrayField = collection.getType().getFieldByCode("array");
            var thatArray = (ArrayInstance) collection.getField(thatArrayField);
            array = new ArrayInstance((ArrayType) arrayField.getType(),
                    new InstanceParentRef(instance, arrayField));
            instance.initField(arrayField, array);
            array.addAll(thatArray);
            return instance;
        }
        else
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
    }

    public Instance ChildList() {
        return List();
    }

    public Instance ChildList(Instance c) {
        return List(c);
    }

    public Instance ReadWriteList() {
        return List();
    }

    public Instance ReadWriteList(Instance c) {
        return List(c);
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

    public BooleanInstance add(Instance instance) {
        array.addElement(instance);
        return Instances.trueInstance();
    }

    public BooleanInstance addAll(Instance c) {
        if(c instanceof ClassInstance collection && collection.isList()) {
            var thatArrayField = collection.getType().getFieldByCode("array");
            var thatArray = (ArrayInstance) collection.getField(thatArrayField);
            array.addAll(thatArray);
            return Instances.trueInstance();
        }
        else
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
    }

    public static ClassInstance of(ClassType type, Instance values) {
        if(values instanceof ArrayInstance array) {
            var list = ClassInstance.allocate(type);
            var listNative = (ListNative) NativeMethods.getNativeObject(list);
            listNative.List();
            array.forEach(listNative::add);
            return list;
        }
        else
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
    }

    public Instance isEmpty() {
        return Instances.booleanInstance(array.isEmpty());
    }

    public LongInstance size() {
        return Instances.longInstance(array.size());
    }

    public ArrayInstance toArray() {
        return array;
    }

}
