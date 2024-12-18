package org.metavm.entity.natives;

import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Value;

public class IllegalStateExceptionNative extends RuntimeExceptionNative {

    public IllegalStateExceptionNative(ClassInstance instance) {
        super(instance);
    }

    public Value IllegalStateException(CallContext callContext) {
        return RuntimeException(callContext);
    }

    public Value IllegalStateException(Value causeOrMessage, CallContext callContext) {
        return RuntimeException(causeOrMessage, callContext);
    }

    public Value IllegalStateException(Value message, Value cause, CallContext callContext) {
        return RuntimeException(message, cause, callContext);
    }

}
