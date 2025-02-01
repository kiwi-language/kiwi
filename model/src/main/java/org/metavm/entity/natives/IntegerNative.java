package org.metavm.entity.natives;

import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.IntValue;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Klass;
import org.metavm.util.Instances;

public class IntegerNative extends IntegerNativeStub {

    public IntegerNative(ClassInstance instance) {
        super(instance);
    }

    public static Value numberOfLeadingZeros(Klass klass, Value v, CallContext callContext) {
        var l = ((IntValue) v).value;
        return Instances.intInstance(Integer.numberOfLeadingZeros(l));
    }

    public static Value numberOfTrailingZeros(Klass klass, Value v, CallContext callContext) {
        var l = ((IntValue) v).value;
        return Instances.intInstance(Integer.numberOfTrailingZeros(l));
    }

}
