package org.metavm.entity.natives;

import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Value;

public class NullPointerExceptionNative extends RuntimeExceptionNative {

    public NullPointerExceptionNative(ClassInstance instance) {
        super(instance);
    }

    public Value NullPointerException(CallContext callContext) {
        return RuntimeException(callContext);
    }

    public Value NullPointerException(Value causeOrMessage, CallContext callContext) {
        return RuntimeException(causeOrMessage, callContext);
    }

    public Value NullPointerException(Value message, Value cause, CallContext callContext) {
        return RuntimeException(message, cause, callContext);
    }

}
