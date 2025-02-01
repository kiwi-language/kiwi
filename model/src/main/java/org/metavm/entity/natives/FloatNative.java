package org.metavm.entity.natives;

import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.FloatValue;
import org.metavm.object.instance.core.IntValue;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Klass;
import org.metavm.util.Instances;

public class FloatNative extends FloatNativeStub {

    public FloatNative(ClassInstance instance) {
        super(instance);
    }

    public static Value floatToIntBits(Klass klass, Value value, CallContext callContext) {
        var f = ((FloatValue) value).value;
        return Instances.intInstance(Float.floatToIntBits(f));
    }

    public static Value floatToRawIntBits(Klass klass, Value value, CallContext callContext) {
        var f = ((FloatValue) value).value;
        return Instances.intInstance(Float.floatToRawIntBits(f));
    }

    public static Value intBitsToFloat(Klass klass, Value value, CallContext callContext) {
        var i = ((IntValue) value).value;
        return Instances.floatInstance(Float.intBitsToFloat(i));
    }

}
