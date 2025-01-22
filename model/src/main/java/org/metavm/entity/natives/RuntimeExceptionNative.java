package org.metavm.entity.natives;

import lombok.extern.slf4j.Slf4j;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Value;

@Slf4j
public class RuntimeExceptionNative extends ExceptionNative {

    public RuntimeExceptionNative(ClassInstance instance) {
        super(instance);
    }

    public Value RuntimeException(CallContext callContext) {
        return Exception(callContext);
    }

    public Value RuntimeException(Value causeOrMessage, CallContext callContext) {
        return Exception(causeOrMessage, callContext);
    }

    public Value RuntimeException(Value message, Value cause, CallContext callContext) {
        return Exception(message, cause, callContext);
    }

}
