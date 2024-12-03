package org.metavm.entity.natives;

import org.metavm.object.instance.core.BooleanValue;
import org.metavm.object.instance.core.IntValue;
import org.metavm.object.instance.core.Value;
import org.metavm.util.InternalException;

public class NativeBase {

    protected int getInt(Value instance) {
        if(instance instanceof IntValue intValue) {
            return intValue.value;
        }
        else {
            throw new InternalException("Index must be a LongInstance, actually got: " + instance);
        }
    }

    protected boolean getBool(Value instance) {
        if(instance instanceof BooleanValue boolInstance) {
            return boolInstance.getValue();
        }
        else {
            throw new InternalException("Index must be a BooleanInstance, actually got: " + instance);
        }
    }

    public void flush() {}

}
