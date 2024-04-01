package tech.metavm.entity.natives;

import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;

public class IllegalStateExceptionNative extends RuntimeExceptionNative {

    public IllegalStateExceptionNative(ClassInstance instance) {
        super(instance);
    }

    public ClassInstance IllegalArgumentException(CallContext callContext) {
        return RuntimeException(callContext);
    }

    public ClassInstance IllegalArgumentException(Instance causeOrMessage, CallContext callContext) {
        return RuntimeException(causeOrMessage, callContext);
    }

    public ClassInstance IllegalArgumentException(Instance message, Instance cause, CallContext callContext) {
        return RuntimeException(message, cause, callContext);
    }

}
