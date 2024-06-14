package org.metavm.entity.natives;

import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.NullInstance;
import org.metavm.object.instance.core.StringInstance;
import org.metavm.object.type.Field;
import org.metavm.util.Instances;
import org.metavm.util.InternalException;

public class ThrowableNative extends NativeBase {

    private final ClassInstance instance;
    private final Field messageField;
    private final Field causeField;

    public ThrowableNative(ClassInstance instance) {
        this.instance = instance;
        messageField = instance.getKlass().getFieldByCode("detailMessage");
        causeField = instance.getKlass().getFieldByCode("cause");
    }

    public ClassInstance Throwable(CallContext callContext) {
        return Throwable(Instances.nullInstance(), Instances.nullInstance(), callContext);
    }

    public ClassInstance Throwable(Instance causeOrMessage, CallContext callContext) {
        if(causeOrMessage instanceof NullInstance nullInstance) {
            return Throwable(nullInstance, nullInstance, callContext);
        }
        else if(causeOrMessage instanceof StringInstance message) {
            return Throwable(message, Instances.nullInstance(), callContext);
        }
        else if(causeOrMessage instanceof ClassInstance cause) {
            return Throwable(Instances.nullInstance(), cause, callContext);
        }
        throw new InternalException("Invalid argument: " + causeOrMessage);
    }

    public ClassInstance Throwable(Instance message, Instance cause, CallContext callContext) {
        instance.initField(messageField, message);
        instance.initField(causeField, cause);
        return instance;
    }

    public Instance getMessage(CallContext callContext) {
        return getMessage();
    }

    public Instance getMessage() {
        return messageField.get(instance);
    }

    public static String getMessage(ClassInstance exception) {
        var n = new ThrowableNative(exception);
        return ((StringInstance) n.getMessage()).getValue();
    }

}
