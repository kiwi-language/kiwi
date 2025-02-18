package org.metavm.entity.natives;

import org.metavm.entity.StdField;
import org.metavm.entity.StdKlass;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.ByteValue;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Klass;
import org.metavm.object.type.PrimitiveType;
import org.metavm.util.Instances;

import java.util.Map;

// Generated code. Do not modify. @see @PrimitiveNativeGenerator
public class ByteNativeStub implements NativeBase {

    private final ClassInstance instance;

    public ByteNativeStub(ClassInstance instance) {
        this.instance = instance;
    }

    public Value compareTo(Value that, CallContext callContext) {
        return Instances.intInstance(
                Byte.compare(getValue(instance), getValue(that))
        );
    }

    public Value equals(Value that, CallContext callContext) {
        return Instances.intInstance(getValue(instance) == getValue(that));
    }

    public Value hashCode(CallContext callContext) {
        return Instances.intInstance(Byte.hashCode(getValue(instance)));
    }

    public Value toString(CallContext callContext) {
        return Instances.stringInstance(Byte.toString(getValue(instance)));
    }

    public static Value compare(Klass klass, Value x, Value y, CallContext callContext) {
        var i = ((ByteValue) x).value;
        var j = ((ByteValue) y).value;
        return Instances.intInstance(Byte.compare(i, j));
    }

    public static Value valueOf__byte(Klass klass, Value value, CallContext callContext) {
        return valueOf((ByteValue) PrimitiveType.byteType.fromStackValue(value));
    }

    public static Value valueOf(ByteValue value) {
        var byteType = StdKlass.byte_.type();
        var valueField = StdField.byteValue.get();
        var data = Map.of(valueField, value);
        return ClassInstance.create(null, data, byteType).getReference();
    }

    private static byte getValue(Value value) {
        return getValue(value.resolveObject());
    }

    private static byte getValue(ClassInstance instance) {
        var i = (ByteValue) instance.getField(StdField.byteValue.get());
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
