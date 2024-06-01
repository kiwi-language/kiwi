package tech.metavm.entity.natives;

import tech.metavm.common.ErrorCode;
import tech.metavm.entity.StandardTypes;
import tech.metavm.object.instance.core.ArrayInstance;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.FunctionInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.ArrayType;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.Klass;
import tech.metavm.object.type.rest.dto.InstanceParentRef;
import tech.metavm.util.BusinessException;
import tech.metavm.util.Instances;
import tech.metavm.util.NncUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SetNative extends IterableNative {

    private final ClassInstance instance;
    private final Map<Instance, Integer> element2index = new HashMap<>();
    private final Field arrayField;
    private ArrayInstance array;

    public SetNative(ClassInstance instance) {
        this.instance = instance;
        arrayField = NncUtils.requireNonNull(instance.getKlass().findFieldByCode("array"));
        if (instance.isFieldInitialized(arrayField)) {
            array = (ArrayInstance) instance.getField(arrayField);
            for (int i = 0; i < array.getElements().size(); i++) {
                element2index.put(array.get(i), i);
            }
        }
    }

    public Instance Set(CallContext callContext) {
        array = new ArrayInstance((ArrayType) arrayField.getType(),
                new InstanceParentRef(instance, arrayField));
        return instance;
    }

    public ClassInstance iterator(CallContext callContext) {
        var iteratorImplType = (Klass) instance.getKlass().getDependency(StandardTypes.getIteratorImplKlass());
        var it = ClassInstance.allocate(iteratorImplType.getType());
        var itNative = (IteratorImplNative) NativeMethods.getNativeObject(it);
        itNative.IteratorImpl(instance, callContext);
        return it;
    }

    public Instance add(Instance value, CallContext callContext) {
        if (!element2index.containsKey(value)) {
            element2index.put(value, array.size());
            array.addElement(value);
            return Instances.trueInstance();
        } else {
            return Instances.falseInstance();
        }
    }

    public Instance remove(Instance value, CallContext callContext) {
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

    public Instance isEmpty(CallContext callContext) {
        return Instances.booleanInstance(element2index.isEmpty());
    }

    public Instance contains(Instance value, CallContext callContext) {
        return Instances.booleanInstance(element2index.containsKey(value));
    }

    public Instance size(CallContext callContext) {
        return Instances.longInstance(element2index.size());
    }

    public void clear(CallContext callContext) {
        array.clear();
        element2index.clear();
    }

    @Override
    public void forEach(Instance action, CallContext callContext) {
        if(action instanceof FunctionInstance functionInstance)
            array.forEach(e -> functionInstance.execute(List.of(e), callContext));
        else
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
    }
}
