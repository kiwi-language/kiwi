package tech.metavm.entity.natives;

import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;

public class IllegalArgumentExceptionNative extends RuntimeExceptionNative {

    public IllegalArgumentExceptionNative(ClassInstance instance) {
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
