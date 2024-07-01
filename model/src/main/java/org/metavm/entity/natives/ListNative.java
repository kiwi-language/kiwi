package org.metavm.entity.natives;

import org.metavm.common.ErrorCode;
import org.metavm.entity.StdKlass;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.ArrayType;
import org.metavm.object.type.Field;
import org.metavm.object.type.Klass;
import org.metavm.object.type.rest.dto.InstanceParentRef;
import org.metavm.util.BusinessException;
import org.metavm.util.Instances;
import org.metavm.util.NncUtils;

import java.util.List;

public class ListNative extends IterableNative {

    private final ClassInstance instance;
    private final Field arrayField;
    private ArrayInstance array;

    public ListNative(ClassInstance instance) {
        this.instance = instance;
        arrayField = NncUtils.requireNonNull(instance.getKlass().findFieldByCode("array"));
        if(instance.isFieldInitialized(arrayField)) {
            array = (ArrayInstance) instance.getField(arrayField);
        }
    }

    public Instance List(CallContext callContext) {
        return List();
    }

    public Instance List() {
        array = new ArrayInstance((ArrayType) arrayField.getType());
        instance.initField(arrayField, array);
        return instance;
    }

    public Instance List(Instance c, CallContext callContext) {
        if(c instanceof ClassInstance collection) {
            var thatArrayField = collection.getKlass().getFieldByCode("array");
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

    public Instance ChildList(CallContext callContext) {
        return List(callContext);
    }

    public Instance ChildList(Instance c, CallContext callContext) {
        return List(c, callContext);
    }

    public Instance ArrayList(CallContext callContext) {
        return List(callContext);
    }

    public Instance ArrayList(Instance c, CallContext callContext) {
        return List(c, callContext);
    }

    public Instance ValueList(CallContext callContext) {
        return List(callContext);
    }

    public Instance ValueList(Instance c, CallContext callContext) {
        return List(c, callContext);
    }

    public ClassInstance iterator(CallContext callContext) {
        var iteratorImplType = StdKlass.iteratorImpl.get().getParameterized(List.of(instance.getKlass().getFirstTypeArgument()));
        var it = ClassInstance.allocate(iteratorImplType.getType());
        var itNative = (IteratorImplNative) NativeMethods.getNativeObject(it);
        itNative.IteratorImpl(instance, callContext);
        return it;
    }

    public Instance get(Instance index, CallContext callContext) {
        return array.get(getInt(index));
    }

    public Instance set(Instance index, Instance value, CallContext callContext) {
        return array.setElement(getInt(index), value);
    }

    public BooleanInstance remove(Instance instance, CallContext callContext) {
        return Instances.booleanInstance(array.removeElement(instance));
    }

    public Instance removeAt(Instance index, CallContext callContext) {
        return array.removeElement(getInt(index));
    }

    public Instance contains(Instance value, CallContext callContext) {
        return Instances.booleanInstance(array.contains(value));
    }

    public void clear(CallContext callContext) {
        clear();
    }

    public void clear() {
        array.clear();
    }

    public BooleanInstance add(Instance instance, CallContext callContext) {
        return add(instance);
    }

    public BooleanInstance add(Instance instance) {
        array.addElement(instance);
        return Instances.trueInstance();
    }

    public BooleanInstance addAll(Instance c, CallContext callContext) {
        if(c instanceof ClassInstance collection && collection.isList()) {
            var thatArrayField = collection.getKlass().getFieldByCode("array");
            var thatArray = (ArrayInstance) collection.getField(thatArrayField);
            array.addAll(thatArray);
            return Instances.trueInstance();
        }
        else
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
    }

    public static ClassInstance of(Klass klass, Instance values, CallContext callContext) {
        if(values instanceof ArrayInstance array) {
            var list = ClassInstance.allocate(klass.getType());
            var listNative = (ListNative) NativeMethods.getNativeObject(list);
            listNative.List(callContext);
            array.forEach(e -> listNative.add(e, callContext));
            return list;
        }
        else
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
    }

    public Instance isEmpty(CallContext callContext) {
        return Instances.booleanInstance(array.isEmpty());
    }

    public LongInstance size(CallContext callContext) {
        return Instances.longInstance(array.size());
    }

    public BooleanInstance removeIf(Instance filter, CallContext callContext) {
        if(filter instanceof ClassInstance classInstance) {
            var method = classInstance.getKlass().getMethods().get(0);
            return Instances.booleanInstance(array.removeIf(e -> method.execute(
                    classInstance, List.of(e), callContext).booleanRet()));
        }
        else
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
    }

    public ArrayInstance toArray() {
        return array;
    }

    @Override
    public void forEach(Instance action, CallContext callContext) {
        if(action instanceof ClassInstance classInstance) {
            var method = classInstance.getKlass().getMethods().get(0);
            array.forEach(e -> method.execute(classInstance, List.of(e), callContext));
        }
        else
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
    }

    public ClassInstance getInstance() {
        return instance;
    }
}
