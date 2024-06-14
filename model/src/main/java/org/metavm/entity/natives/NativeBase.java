package org.metavm.entity.natives;

import org.metavm.object.instance.core.BooleanInstance;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.LongInstance;
import org.metavm.util.InternalException;

public class NativeBase {

    protected int getInt(Instance instance) {
        if(instance instanceof LongInstance longInstance) {
            return longInstance.getValue().intValue();
        }
        else {
            throw new InternalException("Index must be a LongInstance, actually got: " + instance);
        }
    }

    protected boolean getBool(Instance instance) {
        if(instance instanceof BooleanInstance boolInstance) {
            return boolInstance.getValue();
        }
        else {
            throw new InternalException("Index must be a BooleanInstance, actually got: " + instance);
        }
    }

}
