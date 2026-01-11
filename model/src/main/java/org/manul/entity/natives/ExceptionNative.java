package org.manul.entity.natives;

import org.manul.entity.StdField;
import org.manul.object.instance.core.ClassInstance;
import org.manul.object.instance.core.NullValue;
import org.manul.object.instance.core.StringReference;
import org.manul.object.instance.core.Value;
import org.manul.util.Instances;

public class ExceptionNative {

    public static Value Exception(ClassInstance instance, Value causeOrMessage) {
        if(causeOrMessage instanceof NullValue nullInstance)
            return Exception(instance, nullInstance, nullInstance);
        else if(causeOrMessage instanceof StringReference)
            return Exception(instance, causeOrMessage, Instances.nullInstance());
        else
            return Exception(instance, Instances.nullInstance(), causeOrMessage);
    }

    private static Value Exception(ClassInstance instance, Value message, Value cause) {
        instance.initField(StdField.exceptionDetailMessage.get(), message);
        instance.initField(StdField.exceptionCause.get(), cause);
        return instance.getReference();
    }

    public static String getMessage(ClassInstance exception) {
        if (StdField.exceptionDetailMessage.get().get(exception) instanceof StringReference s)
            return s.getValue();
        else
            return null;
    }

}
