package org.metavm.entity.natives;

import org.metavm.entity.StdField;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.LongValue;
import org.metavm.object.instance.core.StringValue;

public class EnumNative extends NativeBase {

    private final ClassInstance instance;

    public EnumNative(ClassInstance instance) {
        this.instance = instance;
    }

    public StringValue name(CallContext callContext) {
        return (StringValue) instance.getField(StdField.enumName.get());
    }

    public LongValue ordinal(CallContext callContext) {
        return (LongValue) instance.getField(StdField.enumOrdinal.get());
    }

}
