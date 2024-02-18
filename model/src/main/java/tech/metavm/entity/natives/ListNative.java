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

import java.util.List;

public class ListNative extends IterableNative {

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

    public Instance List(NativeCallContext callContext) {
        return List();
    }

    public Instance List() {
        array = new ArrayInstance((ArrayType) arrayField.getType());
        instance.initField(arrayField, array);
        return instance;
    }

    public Instance List(Instance c, NativeCallContext callContext) {
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

    public Instance ChildList(NativeCallContext callContext) {
        return List(callContext);
    }

    public Instance ChildList(Instance c, NativeCallContext callContext) {
        return List(c, callContext);
    }

    public Instance ReadWriteList(NativeCallContext callContext) {
        return List(callContext);
    }

    public Instance ReadWriteList(Instance c, NativeCallContext callContext) {
        return List(c, callContext);
    }

    public ClassInstance iterator(NativeCallContext callContext) {
        var iteratorImplType = (ClassType) instance.getType().getDependency(StandardTypes.getIteratorImplType());
        var it = ClassInstance.allocate(iteratorImplType);
        var itNative = (IteratorImplNative) NativeMethods.getNativeObject(it);
        itNative.IteratorImpl(instance, callContext);
        return it;
    }

    public Instance get(Instance index, NativeCallContext callContext) {
        return array.get(getInt(index));
    }

    public Instance set(Instance index, Instance value, NativeCallContext callContext) {
        return array.setElement(getInt(index), value);
    }

    public BooleanInstance remove(Instance instance, NativeCallContext callContext) {
        return Instances.booleanInstance(array.removeElement(instance));
    }

    public Instance removeAt(Instance index, NativeCallContext callContext) {
        return array.removeElement(getInt(index));
    }

    public Instance contains(Instance value, NativeCallContext callContext) {
        return Instances.booleanInstance(array.contains(value));
    }

    public void clear(NativeCallContext callContext) {
        clear();
    }

    public void clear() {
        array.clear();
    }

    public BooleanInstance add(Instance instance, NativeCallContext callContext) {
        return add(instance);
    }

    public BooleanInstance add(Instance instance) {
        array.addElement(instance);
        return Instances.trueInstance();
    }

    public BooleanInstance addAll(Instance c, NativeCallContext callContext) {
        if(c instanceof ClassInstance collection && collection.isList()) {
            var thatArrayField = collection.getType().getFieldByCode("array");
            var thatArray = (ArrayInstance) collection.getField(thatArrayField);
            array.addAll(thatArray);
            return Instances.trueInstance();
        }
        else
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
    }

    public static ClassInstance of(ClassType type, Instance values, NativeCallContext callContext) {
        if(values instanceof ArrayInstance array) {
            var list = ClassInstance.allocate(type);
            var listNative = (ListNative) NativeMethods.getNativeObject(list);
            listNative.List(callContext);
            array.forEach(e -> listNative.add(e, callContext));
            return list;
        }
        else
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
    }

    public Instance isEmpty(NativeCallContext callContext) {
        return Instances.booleanInstance(array.isEmpty());
    }

    public LongInstance size(NativeCallContext callContext) {
        return Instances.longInstance(array.size());
    }

    public ArrayInstance toArray() {
        return array;
    }

    @Override
    public void forEach(Instance action, NativeCallContext callContext) {
        if(action instanceof ClassInstance classInstance) {
            var method = classInstance.getType().getMethods().get(0);
            array.forEach(e -> method.execute(
                    classInstance, List.of(e), callContext.instanceRepository(), callContext.parameterizedFlowProvider()));
        }
        else
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
    }
}
