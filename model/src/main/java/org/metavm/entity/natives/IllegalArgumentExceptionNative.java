package org.metavm.entity.natives;

import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.InstanceReference;

public class IllegalArgumentExceptionNative extends RuntimeExceptionNative {

    public IllegalArgumentExceptionNative(ClassInstance instance) {
        super(instance);
    }

    public InstanceReference IllegalArgumentException(CallContext callContext) {
        return RuntimeException(callContext);
    }

    public InstanceReference IllegalArgumentException(Instance causeOrMessage, CallContext callContext) {
        return RuntimeException(causeOrMessage, callContext);
    }

    public InstanceReference IllegalArgumentException(Instance message, Instance cause, CallContext callContext) {
        return RuntimeException(message, cause, callContext);
    }

}
