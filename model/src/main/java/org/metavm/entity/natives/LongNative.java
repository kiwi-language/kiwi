package org.metavm.entity.natives;

import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.LongValue;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Klass;
import org.metavm.util.Instances;

public class LongNative extends LongNativeStub {

    public LongNative(ClassInstance instance) {
        super(instance);
    }

    public static Value numberOfLeadingZeros(Klass klass, Value v, CallContext callContext) {
        var l = ((LongValue) v).value;
        return Instances.intInstance(Long.numberOfLeadingZeros(l));
    }

    public static Value numberOfTrailingZeros(Klass klass, Value v, CallContext callContext) {
        var l = ((LongValue) v).value;
        return Instances.intInstance(Long.numberOfTrailingZeros(l));
    }

}
