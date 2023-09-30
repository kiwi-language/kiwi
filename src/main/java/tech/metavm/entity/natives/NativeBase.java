package tech.metavm.entity.natives;

import tech.metavm.entity.IInstanceContext;
import tech.metavm.object.instance.BooleanInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.LongInstance;
import tech.metavm.util.InternalException;

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
