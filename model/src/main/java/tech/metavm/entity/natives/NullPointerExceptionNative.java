package tech.metavm.entity.natives;

import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;

public class NullPointerExceptionNative extends RuntimeExceptionNative {

    public NullPointerExceptionNative(ClassInstance instance) {
        super(instance);
    }

    public ClassInstance NullPointerException(CallContext callContext) {
        return RuntimeException(callContext);
    }

    public ClassInstance NullPointerException(Instance causeOrMessage, CallContext callContext) {
        return RuntimeException(causeOrMessage, callContext);
    }

    public ClassInstance NullPointerException(Instance message, Instance cause, CallContext callContext) {
        return RuntimeException(message, cause, callContext);
    }

}
