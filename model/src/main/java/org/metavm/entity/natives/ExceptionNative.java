package org.metavm.entity.natives;

import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;

public class ExceptionNative extends ThrowableNative {

    public ExceptionNative(ClassInstance instance) {
        super(instance);
    }

    public Reference Exception(CallContext callContext) {
        return Throwable(callContext);
    }

    public Reference Exception(Value causeOrMessage, CallContext callContext) {
        return Throwable(causeOrMessage, callContext);
    }

    public Reference Exception(Value message, Value cause, CallContext callContext) {
        return Throwable(message, cause, callContext);
    }

}
