package org.metavm.entity.natives;

import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;

public class IllegalArgumentExceptionNative extends RuntimeExceptionNative {

    public IllegalArgumentExceptionNative(ClassInstance instance) {
        super(instance);
    }

    public Reference IllegalArgumentException(CallContext callContext) {
        return RuntimeException(callContext);
    }

    public Reference IllegalArgumentException(Value causeOrMessage, CallContext callContext) {
        return RuntimeException(causeOrMessage, callContext);
    }

    public Reference IllegalArgumentException(Value message, Value cause, CallContext callContext) {
        return RuntimeException(message, cause, callContext);
    }

}
