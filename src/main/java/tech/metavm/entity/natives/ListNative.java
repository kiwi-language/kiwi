package tech.metavm.entity.natives;

import tech.metavm.entity.IInstanceContext;
import tech.metavm.object.instance.*;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.TypeUtil;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.NncUtils;

public class ListNative extends NativeBase {

    private final ClassInstance list;
    private final Field arrayField;
    private final Type elementType;
    private ArrayInstance array;

    public ListNative(ClassInstance list, IInstanceContext context) {
        super(context);
        this.list = list;
        elementType = list.getType().getTypeArguments().get(0);
        arrayField = NncUtils.requireNonNull(list.getType().getFieldByCode("array"));
        if(list.isFieldInitialized(arrayField)) {
            array = (ArrayInstance) list.get(arrayField);
        }
    }

    public ClassInstance init(Instance elementAsChild) {
        array = new ArrayInstance((ArrayType) arrayField.getType(), getBool(elementAsChild));
        list.initializeField(arrayField, array);
        return list;
    }

    public Instance iterator() {
        var iteratorImplType = TypeUtil.getIteratorImplType(elementType, context.getEntityContext());
        var it = ClassInstance.allocate(iteratorImplType);
        var itNative = (IteratorImplNative) NativeInvoker.getNativeObject(it, context);
        itNative.init(list);
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
