package tech.metavm.entity.natives;

import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;

public class RuntimeExceptionNative extends ExceptionNative {

    public RuntimeExceptionNative(ClassInstance instance) {
        super(instance);
    }

    public ClassInstance RuntimeException() {
        return Exception();
    }

    public ClassInstance RuntimeException(Instance causeOrMessage) {
        return Exception(causeOrMessage);
    }

    public ClassInstance RuntimeException(Instance message, Instance cause) {
        return Exception(message, cause);
    }

}
