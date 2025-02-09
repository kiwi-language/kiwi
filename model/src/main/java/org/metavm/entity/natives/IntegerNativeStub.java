package org.metavm.entity.natives;

import org.metavm.entity.StdField;
import org.metavm.entity.StdKlass;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.IntValue;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Klass;
import org.metavm.object.type.PrimitiveType;
import org.metavm.util.Instances;

import java.util.Map;

// Generated code. Do not modify. @see @PrimitiveNativeGenerator
public class IntegerNativeStub implements NativeBase {

    private final ClassInstance instance;

    public IntegerNativeStub(ClassInstance instance) {
        this.instance = instance;
    }

    public Value compareTo(Value that, CallContext callContext) {
        return Instances.intInstance(
                Integer.compare(getValue(instance), getValue(that))
        );
    }

    public Value equals(Value that, CallContext callContext) {
        return Instances.intInstance(getValue(instance) == getValue(that));
    }

    public Value hashCode(CallContext callContext) {
        return Instances.intInstance(Integer.hashCode(getValue(instance)));
    }

    public Value toString(CallContext callContext) {
        return Instances.stringInstance(Integer.toString(getValue(instance)));
    }

    public static Value compare(Klass klass, Value x, Value y, CallContext callContext) {
        var i = ((IntValue) x).value;
        var j = ((IntValue) y).value;
        return Instances.intInstance(Integer.compare(i, j));
    }

    public static Value valueOf__int(Klass klass, Value value, CallContext callContext) {
        return valueOf((IntValue) value);
    }

    public static Value valueOf(IntValue value) {
        var intType = StdKlass.integer.type();
        var valueField = StdField.integerValue.get();
        var data = Map.of(valueField, value);
        return ClassInstance.create(data, intType).getReference();
    }

    private static int getValue(Value value) {
        return getValue(value.resolveObject());
    }

    private static int getValue(ClassInstance instance) {
        var i = (IntValue) instance.getField(StdField.integerValue.get());
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
