package tech.metavm.entity.natives;

import tech.metavm.object.instance.ArrayInstance;
import tech.metavm.object.instance.BooleanInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.LongInstance;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.InternalException;

public class ArrayNative {

    private final ArrayInstance array;

    public ArrayNative(ArrayInstance array) {
        this.array = array;
    }

    public ArrayInstance init(Instance elementAsChild) {
        if(!(elementAsChild instanceof BooleanInstance bool)) {
            throw new InternalException("elementAsChild must be a BooleanInstance, actually got: " + elementAsChild);
        }
        array.setElementAsChild(bool.isTrue());
        return array;
    }

    public Instance get(Instance index) {
        return array.get(getIndex(index));
    }

    public Instance set(Instance index, Instance value) {
        return array.set(getIndex(index), value);
    }

    public BooleanInstance remove(Instance instance) {
        return InstanceUtils.booleanInstance(array.remove(instance));
    }

    public Instance removeAt(Instance index) {
        return array.remove(getIndex(index));
    }

    private int getIndex(Instance instance) {
        if(instance instanceof LongInstance longInstance) {
            return longInstance.getValue().intValue();
        }
        else {
            throw new InternalException("Index must be a LongInstance, actually got: " + instance);
        }
    }

    public void clear() {
        array.clear();
    }

    public void add(Instance instance) {
        array.add(instance);
    }

    public LongInstance size() {
        return InstanceUtils.longInstance(array.size());
    }

}
