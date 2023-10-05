package tech.metavm.entity.natives;

import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.NullInstance;
import tech.metavm.object.instance.StringInstance;
import tech.metavm.object.meta.Field;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.InternalException;

public class ThrowableNative extends NativeBase {

    private final ClassInstance instance;
    private final Field messageField;
    private final Field causeField;

    public ThrowableNative(ClassInstance instance) {
        this.instance = instance;
        messageField = instance.getType().getFieldByCodeRequired("detailMessage");
        causeField = instance.getType().getFieldByCodeRequired("cause");
    }

    public ClassInstance Throwable() {
        return Throwable(InstanceUtils.nullInstance(), InstanceUtils.nullInstance());
    }

    public ClassInstance Throwable(Instance causeOrMessage) {
        if(causeOrMessage instanceof NullInstance nullInstance) {
            return Throwable(nullInstance, nullInstance);
        }
        else if(causeOrMessage instanceof StringInstance message) {
            return Throwable(message, InstanceUtils.nullInstance());
        }
        else if(causeOrMessage instanceof ClassInstance cause) {
            return Throwable(InstanceUtils.nullInstance(), cause);
        }
        throw new InternalException("Invalid argument: " + causeOrMessage);
    }

    public ClassInstance Throwable(Instance message, Instance cause) {
        instance.initializeField(messageField, message);
        instance.initializeField(causeField, cause);
        return instance;
    }

    public Instance getMessage() {
        return messageField.get(instance);
    }

}
