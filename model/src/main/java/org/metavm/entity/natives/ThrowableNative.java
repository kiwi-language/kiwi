package org.metavm.entity.natives;

import org.metavm.object.instance.core.*;
import org.metavm.object.type.Field;
import org.metavm.util.Instances;
import org.metavm.util.InternalException;

public class ThrowableNative implements NativeBase {

    private final ClassInstance instance;
    private final Field messageField;
    private final Field causeField;

    public ThrowableNative(ClassInstance instance) {
        this.instance = instance;
        messageField = instance.getInstanceKlass().getFieldByName("detailMessage");
        causeField = instance.getInstanceKlass().getFieldByName("cause");
    }

    public Value Throwable(CallContext callContext) {
        return Throwable(Instances.nullInstance(), Instances.nullInstance(), callContext);
    }

    public Value Throwable(Value causeOrMessage, CallContext callContext) {
        if(causeOrMessage instanceof NullValue nullInstance) {
            return Throwable(nullInstance, nullInstance, callContext);
        }
        else if(causeOrMessage instanceof StringReference) {
            return Throwable(causeOrMessage, Instances.nullInstance(), callContext);
        }
        else if(causeOrMessage instanceof Value cause) {
            return Throwable(Instances.nullInstance(), cause, callContext);
        }
        throw new InternalException("Invalid argument: " + causeOrMessage);
    }

    public Value Throwable(Value message, Value cause, CallContext callContext) {
        instance.initField(messageField, message);
        instance.initField(causeField, cause);
        return instance.getReference();
    }

    public Value getMessage(CallContext callContext) {
        return getMessage();
    }

    public Value getMessage() {
        return messageField.get(instance);
    }

    public static String getMessage(ClassInstance exception) {
        var n = new ThrowableNative(exception);
        if (n.getMessage() instanceof StringReference s)
            return s.getValue();
        else
            return null;
    }

}
