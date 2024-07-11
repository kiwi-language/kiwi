package org.metavm.entity.natives;

import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.InstanceReference;

public class NullPointerExceptionNative extends RuntimeExceptionNative {

    public NullPointerExceptionNative(ClassInstance instance) {
        super(instance);
    }

    public InstanceReference NullPointerException(CallContext callContext) {
        return RuntimeException(callContext);
    }

    public InstanceReference NullPointerException(Instance causeOrMessage, CallContext callContext) {
        return RuntimeException(causeOrMessage, callContext);
    }

    public InstanceReference NullPointerException(Instance message, Instance cause, CallContext callContext) {
        return RuntimeException(message, cause, callContext);
    }

}
