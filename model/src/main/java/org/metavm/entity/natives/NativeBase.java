package org.metavm.entity.natives;

import org.metavm.object.instance.core.BooleanValue;
import org.metavm.object.instance.core.LongValue;
import org.metavm.object.instance.core.Value;
import org.metavm.util.InternalException;

public class NativeBase {

    protected int getInt(Value instance) {
        if(instance instanceof LongValue longInstance) {
            return longInstance.getValue().intValue();
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

}
