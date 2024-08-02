package org.metavm.entity.natives;

import org.metavm.common.ErrorCode;
import org.metavm.entity.StdKlass;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.ArrayType;
import org.metavm.object.type.Field;
import org.metavm.util.BusinessException;
import org.metavm.util.Instances;
import org.metavm.util.NncUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SetNative extends IterableNative {

    private final ClassInstance instance;
    private final Map<Value, Integer> element2index = new HashMap<>();
    private final Field arrayField;
    private ArrayInstance array;

    public SetNative(ClassInstance instance) {
        this.instance = instance;
        arrayField = NncUtils.requireNonNull(instance.getKlass().findFieldByCode("array"));
        if (instance.isFieldInitialized(arrayField)) {
            array = instance.getField(arrayField).resolveArray();
            for (int i = 0; i < array.getElements().size(); i++) {
                element2index.put(array.get(i), i);
            }
        }
    }

    public Value HashSet(CallContext callContext) {
        array = new ArrayInstance((ArrayType) arrayField.getType());
        instance.initField(arrayField, array.getReference());
        return instance.getReference();
    }

    public Reference iterator(CallContext callContext) {
        var iteratorImplType = StdKlass.iteratorImpl.get().getParameterized(List.of(instance.getKlass().getFirstTypeArgument()));
        var it = ClassInstance.allocate(iteratorImplType.getType());
        var itNative = (IteratorImplNative) NativeMethods.getNativeObject(it);
        itNative.IteratorImpl(instance, callContext);
        return it.getReference();
    }

    public Value add(Value value, CallContext callContext) {
        if (!element2index.containsKey(value)) {
            element2index.put(value, array.size());
            array.addElement(value);
            return Instances.trueInstance();
        } else {
            return Instances.falseInstance();
        }
    }

    public Value remove(Value value, CallContext callContext) {
        Integer index = element2index.remove(value);
        if (index != null) {
            int lastIdx = array.size() - 1;
            var last = array.removeElement(lastIdx);
            if (index != lastIdx) {
                array.setElement(index, last);
                element2index.put(last, index);
            }
            return Instances.trueInstance();
        } else {
            return Instances.falseInstance();
        }
    }

    public Value isEmpty(CallContext callContext) {
        return Instances.booleanInstance(element2index.isEmpty());
    }

    public Value contains(Value value, CallContext callContext) {
        return Instances.booleanInstance(element2index.containsKey(value));
    }

    public Value size(CallContext callContext) {
        return Instances.longInstance(element2index.size());
    }

    public void clear(CallContext callContext) {
        array.clear();
        element2index.clear();
    }

    @Override
    public void forEach(Value action, CallContext callContext) {
        if(action instanceof FunctionValue functionValue)
            array.forEach(e -> functionValue.execute(List.of(e), callContext));
        else
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
    }
}
