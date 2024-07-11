package org.metavm.entity.natives;

import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.InstanceReference;

public class RuntimeExceptionNative extends ExceptionNative {

    public RuntimeExceptionNative(ClassInstance instance) {
        super(instance);
    }

    public InstanceReference RuntimeException(CallContext callContext) {
        return Exception(callContext);
    }

    public InstanceReference RuntimeException(Instance causeOrMessage, CallContext callContext) {
        return Exception(causeOrMessage, callContext);
    }

    public InstanceReference RuntimeException(Instance message, Instance cause, CallContext callContext) {
        return Exception(message, cause, callContext);
    }

}
