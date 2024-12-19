package org.metavm.entity.natives;

import org.metavm.entity.StdField;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Value;

public class EnumNative extends NativeBase {

    private final ClassInstance instance;

    public EnumNative(ClassInstance instance) {
        this.instance = instance;
    }

    public Value Enum(Value name, Value ordinal, CallContext callContext) {
        instance.setField(StdField.enumName.get(), name);
        instance.setField(StdField.enumOrdinal.get(), ordinal);
        return instance.getReference();
    }

    public Value name(CallContext callContext) {
        return instance.getField(StdField.enumName.get());
    }

    public Value ordinal(CallContext callContext) {
        return instance.getField(StdField.enumOrdinal.get());
    }

}
