package org.metavm.entity.natives;

import org.metavm.object.instance.core.*;
import org.metavm.util.Instances;

public class ArrayIndexOutOfBoundsExceptionNative extends IndexOutOfBoundsExceptionNative {

    public ArrayIndexOutOfBoundsExceptionNative(ClassInstance instance) {
        super(instance);
    }

    public Reference ArrayIndexOutOfBoundsException(CallContext callContext) {
        return RuntimeException(callContext);
    }

    public Reference ArrayIndexOutOfBoundsException(Value causeOrMessage, CallContext callContext) {
        if(causeOrMessage instanceof IntValue index) {
            return IndexOutOfBoundsException(
                    Instances.stringInstance("Array index out of range: " + index.value),
                    callContext
            );
        }
        else
            return IndexOutOfBoundsException(causeOrMessage, callContext);
    }

    public Reference ArrayIndexOutOfBoundsException(Value message, Value cause, CallContext callContext) {
        return IndexOutOfBoundsException(message, cause, callContext);
    }

}
