package org.metavm.entity.natives;

import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;

public class NullPointerExceptionNative extends RuntimeExceptionNative {

    public NullPointerExceptionNative(ClassInstance instance) {
        super(instance);
    }

    public Reference NullPointerException(CallContext callContext) {
        return RuntimeException(callContext);
    }

    public Reference NullPointerException(Value causeOrMessage, CallContext callContext) {
        return RuntimeException(causeOrMessage, callContext);
    }

    public Reference NullPointerException(Value message, Value cause, CallContext callContext) {
        return RuntimeException(message, cause, callContext);
    }

}
