package tech.metavm.entity.natives;

import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;

public class IllegalStateExceptionNative extends RuntimeExceptionNative {

    public IllegalStateExceptionNative(ClassInstance instance) {
        super(instance);
    }

    public ClassInstance IllegalArgumentException(NativeCallContext callContext) {
        return RuntimeException(callContext);
    }

    public ClassInstance IllegalArgumentException(Instance causeOrMessage, NativeCallContext callContext) {
        return RuntimeException(causeOrMessage, callContext);
    }

    public ClassInstance IllegalArgumentException(Instance message, Instance cause, NativeCallContext callContext) {
        return RuntimeException(message, cause, callContext);
    }

}
