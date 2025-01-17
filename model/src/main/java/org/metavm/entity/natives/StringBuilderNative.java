package org.metavm.entity.natives;

import org.metavm.object.instance.core.*;
import org.metavm.object.type.ArrayType;
import org.metavm.object.type.Field;
import org.metavm.object.type.PrimitiveType;
import org.metavm.util.Instances;

import java.util.Objects;

public class StringBuilderNative extends NativeBase {

    private final ClassInstance instance;
    private final Field arrayField;
    private ArrayInstance array;

    public StringBuilderNative(ClassInstance instance) {
        this.instance = instance;
        arrayField = Objects.requireNonNull(instance.getInstanceKlass().findFieldByName("array"));
        if(instance.isFieldInitialized(arrayField)) {
            array = instance.getField(arrayField).resolveArray();
        }
    }

    public Value StringBuilder(CallContext callContext) {
        return StringBuilder();
    }

    public Value StringBuilder__string(Value value, CallContext callContext) {
        var sb = StringBuilder();
        append(value, callContext);
        return sb;
    }

    public Value StringBuilder() {
        array = new ArrayInstance((ArrayType) arrayField.getType());
        instance.initField(arrayField, array.getReference());
        return instance.getReference();
    }

    public Value append(Value value, CallContext callContext) {
        array.addElement(value);
        return instance.getReference();
    }

    public Value append__char(Value value, CallContext callContext) {
        return append(PrimitiveType.charType.fromStackValue(value), callContext);
    }

    public Value append__boolean(Value value, CallContext callContext) {
        return append(PrimitiveType.booleanType.fromStackValue(value), callContext);
    }

    public Value append__int(Value value, CallContext callContext) {
        return append(value, callContext);
    }

    public Value append__long(Value value, CallContext callContext) {
        return append(value, callContext);
    }

    public Value append__double(Value value, CallContext callContext) {
        return append(value, callContext);
    }

    public Value append__float(Value value, CallContext callContext) {
        return append(value, callContext);
    }

    public Value append__string(Value value, CallContext callContext) {
        return append(value, callContext);
    }

    public Value append__Any(Value value, CallContext callContext) {
        return append(value, callContext);
    }

    public Value isEmpty(CallContext callContext) {
        return Instances.intInstance(array.isEmpty());
    }

    public Value length(CallContext callContext) {
        return Instances.intInstance(array.length());
    }

    public Value toString(CallContext callContext) {
        var sb = new StringBuilder();
        array.forEach(v -> sb.append(Instances.toString(v, callContext)));
        return Instances.stringInstance(sb.toString());
    }

}
