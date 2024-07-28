package org.metavm.entity.natives;

import org.metavm.entity.StdField;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.LongInstance;
import org.metavm.object.instance.core.StringInstance;

public class EnumNative extends NativeBase {

    private final ClassInstance instance;

    public EnumNative(ClassInstance instance) {
        this.instance = instance;
    }

    public StringInstance name(CallContext callContext) {
        return (StringInstance) instance.getField(StdField.enumName.get());
    }

    public LongInstance ordinal(CallContext callContext) {
        return (LongInstance) instance.getField(StdField.enumOrdinal.get());
    }

}
