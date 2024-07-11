package org.metavm.entity.natives;

import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.InstanceReference;

public class ExceptionNative extends ThrowableNative {

    public ExceptionNative(ClassInstance instance) {
        super(instance);
    }

    public InstanceReference Exception(CallContext callContext) {
        return Throwable(callContext);
    }

    public InstanceReference Exception(Instance causeOrMessage, CallContext callContext) {
        return Throwable(causeOrMessage, callContext);
    }

    public InstanceReference Exception(Instance message, Instance cause, CallContext callContext) {
        return Throwable(message, cause, callContext);
    }

}
