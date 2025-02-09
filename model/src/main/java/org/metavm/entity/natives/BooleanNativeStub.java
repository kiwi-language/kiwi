package org.metavm.entity.natives;

import org.metavm.entity.StdField;
import org.metavm.entity.StdKlass;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.BooleanValue;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Klass;
import org.metavm.object.type.PrimitiveType;
import org.metavm.util.Instances;

import java.util.Map;

// Generated code. Do not modify. @see @PrimitiveNativeGenerator
public class BooleanNativeStub implements NativeBase {

    private final ClassInstance instance;

    public BooleanNativeStub(ClassInstance instance) {
        this.instance = instance;
    }

    public Value compareTo(Value that, CallContext callContext) {
        return Instances.intInstance(
                Boolean.compare(getValue(instance), getValue(that))
        );
    }

    public Value equals(Value that, CallContext callContext) {
        return Instances.intInstance(getValue(instance) == getValue(that));
    }

    public Value hashCode(CallContext callContext) {
        return Instances.intInstance(Boolean.hashCode(getValue(instance)));
    }

    public Value toString(CallContext callContext) {
        return Instances.stringInstance(Boolean.toString(getValue(instance)));
    }

    public static Value compare(Klass klass, Value x, Value y, CallContext callContext) {
        var i = ((BooleanValue) x).value;
        var j = ((BooleanValue) y).value;
        return Instances.intInstance(Boolean.compare(i, j));
    }

    public static Value valueOf__boolean(Klass klass, Value value, CallContext callContext) {
        return valueOf((BooleanValue) PrimitiveType.booleanType.fromStackValue(value));
    }

    public static Value valueOf(BooleanValue value) {
        var booleanType = StdKlass.boolean_.type();
        var valueField = StdField.booleanValue.get();
        var data = Map.of(valueField, value);
        return ClassInstance.create(data, booleanType).getReference();
    }

    private static boolean getValue(Value value) {
        return getValue(value.resolveObject());
    }

    private static boolean getValue(ClassInstance instance) {
        var i = (BooleanValue) instance.getField(StdField.booleanValue.get());
        return i.value;
    }
    
    public Value booleanValue(CallContext callContext) {
        return instance.getField(StdField.booleanValue.get()).toStackValue();
    }

}
