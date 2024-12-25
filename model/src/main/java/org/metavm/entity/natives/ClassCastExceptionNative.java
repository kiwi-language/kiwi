package org.metavm.entity.natives;

import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Value;

public class ClassCastExceptionNative extends RuntimeExceptionNative {

    public ClassCastExceptionNative(ClassInstance instance) {
        super(instance);
    }

    public Value ClassCastException(CallContext callContext) {
        return RuntimeException(callContext);
    }

    public Value ClassCastException(Value causeOrMessage, CallContext callContext) {
        return RuntimeException(causeOrMessage, callContext);
    }

    public Value ClassCastException(Value message, Value cause, CallContext callContext) {
        return RuntimeException(message, cause, callContext);
    }

}
