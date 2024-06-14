package org.metavm.entity.natives;

import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Instance;

public class ExceptionNative extends ThrowableNative {

    public ExceptionNative(ClassInstance instance) {
        super(instance);
    }

    public ClassInstance Exception(CallContext callContext) {
        return Throwable(callContext);
    }

    public ClassInstance Exception(Instance causeOrMessage, CallContext callContext) {
        return Throwable(causeOrMessage, callContext);
    }

    public ClassInstance Exception(Instance message, Instance cause, CallContext callContext) {
        return Throwable(message, cause, callContext);
    }

}
