package org.metavm.entity.natives;

import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.IntValue;
import org.metavm.object.instance.core.Value;
import org.metavm.util.Instances;

public class ArrayIndexOutOfBoundsExceptionNative extends IndexOutOfBoundsExceptionNative {

    public ArrayIndexOutOfBoundsExceptionNative(ClassInstance instance) {
        super(instance);
    }

    public Value ArrayIndexOutOfBoundsException(CallContext callContext) {
        return RuntimeException(callContext);
    }

    public Value ArrayIndexOutOfBoundsException__int(Value index, CallContext callContext) {
        return IndexOutOfBoundsException(
                Instances.stringInstance("Array index out of range: " + ((IntValue) index).value),
                callContext
        );
    }

    public Value ArrayIndexOutOfBoundsException(Value causeOrMessage, CallContext callContext) {
        if(causeOrMessage instanceof IntValue index) {
            return IndexOutOfBoundsException(
                    Instances.stringInstance("Array index out of range: " + index.value),
                    callContext
            );
        }
        else
            return IndexOutOfBoundsException(causeOrMessage, callContext);
    }

    public Value ArrayIndexOutOfBoundsException(Value message, Value cause, CallContext callContext) {
        return IndexOutOfBoundsException(message, cause, callContext);
    }

}
