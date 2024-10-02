package org.metavm.entity.natives;

import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;

public class IndexOutOfBoundsExceptionNative extends RuntimeExceptionNative {

    public IndexOutOfBoundsExceptionNative(ClassInstance instance) {
        super(instance);
    }

    public Reference IndexOutOfBoundsException(CallContext callContext) {
        return RuntimeException(callContext);
    }

    public Reference IndexOutOfBoundsException(Value causeOrMessage, CallContext callContext) {
        return RuntimeException(causeOrMessage, callContext);
    }

    public Reference IndexOutOfBoundsException(Value message, Value cause, CallContext callContext) {
        return RuntimeException(message, cause, callContext);
    }

}
