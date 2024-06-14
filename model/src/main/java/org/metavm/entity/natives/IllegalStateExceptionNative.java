package org.metavm.entity.natives;

import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Instance;

public class IllegalStateExceptionNative extends RuntimeExceptionNative {

    public IllegalStateExceptionNative(ClassInstance instance) {
        super(instance);
    }

    public ClassInstance IllegalStateException(CallContext callContext) {
        return RuntimeException(callContext);
    }

    public ClassInstance IllegalStateException(Instance causeOrMessage, CallContext callContext) {
        return RuntimeException(causeOrMessage, callContext);
    }

    public ClassInstance IllegalStateException(Instance message, Instance cause, CallContext callContext) {
        return RuntimeException(message, cause, callContext);
    }

}
