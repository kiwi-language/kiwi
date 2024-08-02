package org.metavm.entity.natives;

import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;

public class IllegalStateExceptionNative extends RuntimeExceptionNative {

    public IllegalStateExceptionNative(ClassInstance instance) {
        super(instance);
    }

    public Reference IllegalStateException(CallContext callContext) {
        return RuntimeException(callContext);
    }

    public Reference IllegalStateException(Value causeOrMessage, CallContext callContext) {
        return RuntimeException(causeOrMessage, callContext);
    }

    public Reference IllegalStateException(Value message, Value cause, CallContext callContext) {
        return RuntimeException(message, cause, callContext);
    }

}
