package org.metavm.entity.natives;

import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Value;

public class ArithmeticExceptionNative extends RuntimeExceptionNative {

    public ArithmeticExceptionNative(ClassInstance instance) {
        super(instance);
    }

    public Value ArithmeticException(CallContext callContext) {
        return RuntimeException(callContext);
    }

    public Value ArithmeticException(Value causeOrMessage, CallContext callContext) {
        return RuntimeException(causeOrMessage, callContext);
    }

    public Value ArithmeticException(Value message, Value cause, CallContext callContext) {
        return RuntimeException(message, cause, callContext);
    }

}
