package org.metavm.entity.natives;

import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.InstanceReference;

public class IllegalStateExceptionNative extends RuntimeExceptionNative {

    public IllegalStateExceptionNative(ClassInstance instance) {
        super(instance);
    }

    public InstanceReference IllegalStateException(CallContext callContext) {
        return RuntimeException(callContext);
    }

    public InstanceReference IllegalStateException(Instance causeOrMessage, CallContext callContext) {
        return RuntimeException(causeOrMessage, callContext);
    }

    public InstanceReference IllegalStateException(Instance message, Instance cause, CallContext callContext) {
        return RuntimeException(message, cause, callContext);
    }

}
