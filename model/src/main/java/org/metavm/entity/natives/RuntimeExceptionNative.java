package org.metavm.entity.natives;

import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;

public class RuntimeExceptionNative extends ExceptionNative {

    public RuntimeExceptionNative(ClassInstance instance) {
        super(instance);
    }

    public Reference RuntimeException(CallContext callContext) {
        return Exception(callContext);
    }

    public Reference RuntimeException(Value causeOrMessage, CallContext callContext) {
        return Exception(causeOrMessage, callContext);
    }

    public Reference RuntimeException(Value message, Value cause, CallContext callContext) {
        return Exception(message, cause, callContext);
    }

}
