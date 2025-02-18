package org.metavm.entity.natives;

import org.metavm.entity.StdField;
import org.metavm.entity.StdKlass;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.CharValue;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Klass;
import org.metavm.object.type.PrimitiveType;
import org.metavm.util.Instances;

import java.util.Map;

// Generated code. Do not modify. @see @PrimitiveNativeGenerator
public class CharacterNativeStub implements NativeBase {

    private final ClassInstance instance;

    public CharacterNativeStub(ClassInstance instance) {
        this.instance = instance;
    }

    public Value compareTo(Value that, CallContext callContext) {
        return Instances.intInstance(
                Character.compare(getValue(instance), getValue(that))
        );
    }

    public Value equals(Value that, CallContext callContext) {
        return Instances.intInstance(getValue(instance) == getValue(that));
    }

    public Value hashCode(CallContext callContext) {
        return Instances.intInstance(Character.hashCode(getValue(instance)));
    }

    public Value toString(CallContext callContext) {
        return Instances.stringInstance(Character.toString(getValue(instance)));
    }

    public static Value compare(Klass klass, Value x, Value y, CallContext callContext) {
        var i = ((CharValue) x).value;
        var j = ((CharValue) y).value;
        return Instances.intInstance(Character.compare(i, j));
    }

    public static Value valueOf(Klass klass, Value value, CallContext callContext) {
        return valueOf((CharValue) PrimitiveType.charType.fromStackValue(value));
    }

    public static Value valueOf(CharValue value) {
        var charType = StdKlass.character.type();
        var valueField = StdField.characterValue.get();
        var data = Map.of(valueField, value);
        return ClassInstance.create(null, data, charType).getReference();
    }

    private static char getValue(Value value) {
        return getValue(value.resolveObject());
    }

    private static char getValue(ClassInstance instance) {
        var i = (CharValue) instance.getField(StdField.characterValue.get());
        return i.value;
    }
    
    public Value charValue(CallContext callContext) {
        return instance.getField(StdField.characterValue.get()).toStackValue();
    }

}
