package tech.metavm.entity.natives;

import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;

public class ExceptionNative extends ThrowableNative {

    public ExceptionNative(ClassInstance instance) {
        super(instance);
    }

    public ClassInstance Exception() {
        return Throwable();
    }

    public ClassInstance Exception(Instance causeOrMessage) {
        return Throwable(causeOrMessage);
    }

    public ClassInstance Exception(Instance message, Instance cause) {
        return Throwable(message, cause);
    }

}
