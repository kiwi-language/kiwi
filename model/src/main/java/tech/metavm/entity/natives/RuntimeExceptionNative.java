package tech.metavm.entity.natives;

import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;

public class RuntimeExceptionNative extends ExceptionNative {

    public RuntimeExceptionNative(ClassInstance instance) {
        super(instance);
    }

    public ClassInstance RuntimeException(CallContext callContext) {
        return Exception(callContext);
    }

    public ClassInstance RuntimeException(Instance causeOrMessage, CallContext callContext) {
        return Exception(causeOrMessage, callContext);
    }

    public ClassInstance RuntimeException(Instance message, Instance cause, CallContext callContext) {
        return Exception(message, cause, callContext);
    }

}
