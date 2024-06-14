package org.metavm.entity.natives;

import org.metavm.object.instance.core.ArrayInstance;
import org.metavm.object.instance.core.BooleanInstance;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.LongInstance;
import org.metavm.util.Instances;
import org.metavm.util.InternalException;

public class ArrayNative {

    private final ArrayInstance array;

    public ArrayNative(ArrayInstance array) {
        this.array = array;
    }

    public ArrayInstance init() {
        return array;
    }

    public Instance get(Instance index) {
        return array.get(getIndex(index));
    }

    public Instance set(Instance index, Instance value) {
        return array.setElement(getIndex(index), value);
    }

    public BooleanInstance remove(Instance instance) {
        return Instances.booleanInstance(array.removeElement(instance));
    }

    public Instance removeAt(Instance index) {
        return array.removeElement(getIndex(index));
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
        array.addElement(instance);
    }

    public LongInstance size() {
        return Instances.longInstance(array.size());
    }

}
