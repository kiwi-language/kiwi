package tech.metavm.entity.natives;

import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;

public class RuntimeExceptionNative extends ExceptionNative {

    public RuntimeExceptionNative(ClassInstance instance) {
        super(instance);
    }

    public ClassInstance RuntimeException(NativeCallContext callContext) {
        return Exception(callContext);
    }

    public ClassInstance RuntimeException(Instance causeOrMessage, NativeCallContext callContext) {
        return Exception(causeOrMessage, callContext);
    }

    public ClassInstance RuntimeException(Instance message, Instance cause, NativeCallContext callContext) {
        return Exception(message, cause, callContext);
    }

}
