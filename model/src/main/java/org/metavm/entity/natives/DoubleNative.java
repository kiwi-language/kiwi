package org.metavm.entity.natives;

import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.DoubleValue;
import org.metavm.object.instance.core.LongValue;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Klass;
import org.metavm.util.Instances;

public class DoubleNative extends DoubleNativeStub {

    public DoubleNative(ClassInstance instance) {
        super(instance);
    }

    public static Value doubleToLongBits(Klass klass, Value value, CallContext callContext) {
        var d = ((DoubleValue) value).value;
        return Instances.longInstance(Double.doubleToLongBits(d));
    }

    public static Value doubleToRawLongBits(Klass klass, Value value, CallContext callContext) {
        var d = ((DoubleValue) value).value;
        return Instances.longInstance(Double.doubleToRawLongBits(d));
    }

    public static Value longBitsToDouble(Klass klass, Value value, CallContext callContext) {
        var l = ((LongValue) value).value;
        return Instances.doubleInstance(Double.longBitsToDouble(l));
    }

}
