package org.metavm.entity.natives;

import org.metavm.entity.StdField;
import org.metavm.entity.StdKlass;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.LongValue;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Klass;
import org.metavm.object.type.PrimitiveType;
import org.metavm.util.Instances;

import java.util.Map;

// Generated code. Do not modify. @see @PrimitiveNativeGenerator
public class LongNativeStub extends NativeBase {

    private final ClassInstance instance;

    public LongNativeStub(ClassInstance instance) {
        this.instance = instance;
    }

    public Value compareTo(Value that, CallContext callContext) {
        return Instances.intInstance(
                Long.compare(getValue(instance), getValue(that))
        );
    }

    public Value equals(Value that, CallContext callContext) {
        return Instances.intInstance(getValue(instance) == getValue(that));
    }

    public Value hashCode(CallContext callContext) {
        return Instances.intInstance(Long.hashCode(getValue(instance)));
    }

    public Value toString(CallContext callContext) {
        return Instances.stringInstance(Long.toString(getValue(instance)));
    }

    public static Value compare(Klass klass, Value x, Value y, CallContext callContext) {
        var i = ((LongValue) x).value;
        var j = ((LongValue) y).value;
        return Instances.intInstance(Long.compare(i, j));
    }

    public static Value valueOf__long(Klass klass, Value value, CallContext callContext) {
        return valueOf((LongValue) value);
    }

    public static Value valueOf(LongValue value) {
        var longType = StdKlass.long_.type();
        var valueField = StdField.longValue.get();
        var data = Map.of(valueField, value);
        return ClassInstance.create(data, longType).getReference();
    }

    private static long getValue(Value value) {
        return getValue(value.resolveObject());
    }

    private static long getValue(ClassInstance instance) {
        var i = (LongValue) instance.getField(StdField.longValue.get());
        return i.value;
    }
    
    public Value byteValue(CallContext callContext) {
        return Instances.intInstance((byte) getValue(instance));
    }

    public Value shortValue(CallContext callContext) {
        return Instances.intInstance((short) getValue(instance));
    }

    public Value intValue(CallContext callContext) {
        return Instances.intInstance((int) getValue(instance));
    }

    public Value longValue(CallContext callContext) {
        return Instances.longInstance((long) getValue(instance));
    }

    public Value floatValue(CallContext callContext) {
        return Instances.floatInstance((float) getValue(instance));
    }

    public Value doubleValue(CallContext callContext) {
        return Instances.doubleInstance((double) getValue(instance));
    }


}
