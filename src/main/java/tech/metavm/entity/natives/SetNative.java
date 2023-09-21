package tech.metavm.entity.natives;

import tech.metavm.entity.IInstanceContext;
import tech.metavm.object.instance.ArrayInstance;
import tech.metavm.object.instance.ArrayType;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.TypeUtil;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.NncUtils;

import java.util.HashMap;
import java.util.Map;

public class SetNative extends NativeBase {

    private final ClassInstance instance;
    private final Map<Instance, Integer> element2index = new HashMap<>();
    private final Field arrayField;
    private final Type elementType;
    private ArrayInstance array;

    public SetNative(ClassInstance instance, IInstanceContext context) {
        super(context);
        this.instance = instance;
        arrayField = NncUtils.requireNonNull(instance.getType().getFieldByCode("array"));
        elementType = instance.getType().getTypeArguments().get(0);
        if(instance.isFieldInitialized(arrayField)) {
            array = (ArrayInstance) instance.get(arrayField);
        }
    }

    public Instance iterator() {
        var iteratorImplType = TypeUtil.getIteratorImplType(elementType, context.getEntityContext());
        var it = ClassInstance.allocate(iteratorImplType);
        var itNative = (IteratorImplNative) NativeInvoker.getNativeObject(it, context);
        itNative.init(instance);
        return it;
    }

    public Instance init(Instance elementAsChild) {
        array = new ArrayInstance((ArrayType) arrayField.getType(), getBool(elementAsChild));
        instance.initializeField(arrayField, array);
        return instance;
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
