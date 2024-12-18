package org.metavm.entity.natives;

import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Value;

public class IndexOutOfBoundsExceptionNative extends RuntimeExceptionNative {

    public IndexOutOfBoundsExceptionNative(ClassInstance instance) {
        super(instance);
    }

    public Value IndexOutOfBoundsException(CallContext callContext) {
        return RuntimeException(callContext);
    }

    public Value IndexOutOfBoundsException(Value causeOrMessage, CallContext callContext) {
        return RuntimeException(causeOrMessage, callContext);
    }

    public Value IndexOutOfBoundsException(Value message, Value cause, CallContext callContext) {
        return RuntimeException(message, cause, callContext);
    }

}
