package org.metavm.entity.natives;

import org.metavm.object.instance.core.ArrayInstance;
import org.metavm.object.instance.core.BooleanValue;
import org.metavm.object.instance.core.LongValue;
import org.metavm.object.instance.core.Value;
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

    public Value get(Value index) {
        return array.get(getIndex(index));
    }

    public Value set(Value index, Value value) {
        return array.setElement(getIndex(index), value);
    }

    public BooleanValue remove(Value instance) {
        return Instances.booleanInstance(array.removeElement(instance));
    }

    public Value removeAt(Value index) {
        return array.removeElement(getIndex(index));
    }

    private int getIndex(Value instance) {
        if(instance instanceof LongValue longInstance) {
            return longInstance.getValue().intValue();
        }
        else {
            throw new InternalException("Index must be a LongInstance, actually got: " + instance);
        }
    }

    public void clear() {
        array.clear();
    }

    public void add(Value instance) {
        array.addElement(instance);
    }

    public LongValue size() {
        return Instances.longInstance(array.size());
    }

}
