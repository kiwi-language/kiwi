package tech.metavm.entity.natives;

import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.core.NullInstance;
import tech.metavm.object.instance.core.StringInstance;
import tech.metavm.object.type.Field;
import tech.metavm.util.Instances;
import tech.metavm.util.InternalException;

public class ThrowableNative extends NativeBase {

    private final ClassInstance instance;
    private final Field messageField;
    private final Field causeField;

    public ThrowableNative(ClassInstance instance) {
        this.instance = instance;
        messageField = instance.getType().getFieldByCode("detailMessage");
        causeField = instance.getType().getFieldByCode("cause");
    }

    public ClassInstance Throwable() {
        return Throwable(Instances.nullInstance(), Instances.nullInstance());
    }

    public ClassInstance Throwable(Instance causeOrMessage) {
        if(causeOrMessage instanceof NullInstance nullInstance) {
            return Throwable(nullInstance, nullInstance);
        }
        else if(causeOrMessage instanceof StringInstance message) {
            return Throwable(message, Instances.nullInstance());
        }
        else if(causeOrMessage instanceof ClassInstance cause) {
            return Throwable(Instances.nullInstance(), cause);
        }
        throw new InternalException("Invalid argument: " + causeOrMessage);
    }

    public ClassInstance Throwable(Instance message, Instance cause) {
        instance.initField(messageField, message);
        instance.initField(causeField, cause);
        return instance;
    }

    public Instance getMessage() {
        return messageField.get(instance);
    }

}
