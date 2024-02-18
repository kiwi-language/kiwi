package tech.metavm.entity.natives;

import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;

public class ExceptionNative extends ThrowableNative {

    public ExceptionNative(ClassInstance instance) {
        super(instance);
    }

    public ClassInstance Exception(NativeCallContext callContext) {
        return Throwable(callContext);
    }

    public ClassInstance Exception(Instance causeOrMessage, NativeCallContext callContext) {
        return Throwable(causeOrMessage, callContext);
    }

    public ClassInstance Exception(Instance message, Instance cause, NativeCallContext callContext) {
        return Throwable(message, cause, callContext);
    }

}
