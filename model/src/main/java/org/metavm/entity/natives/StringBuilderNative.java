package org.metavm.entity.natives;

import org.metavm.object.instance.core.*;
import org.metavm.object.type.ArrayType;
import org.metavm.object.type.Field;
import org.metavm.util.Instances;
import org.metavm.util.NncUtils;

public class StringBuilderNative extends NativeBase {

    private final ClassInstance instance;
    private final Field arrayField;
    private ArrayInstance array;

    public StringBuilderNative(ClassInstance instance) {
        this.instance = instance;
        arrayField = NncUtils.requireNonNull(instance.getKlass().findFieldByName("array"));
        if(instance.isFieldInitialized(arrayField)) {
            array = instance.getField(arrayField).resolveArray();
        }
    }

    public Value StringBuilder(CallContext callContext) {
        return StringBuilder();
    }

    public Value StringBuilder(Value value, CallContext callContext) {
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

    public BooleanValue isEmpty(CallContext callContext) {
        return Instances.booleanInstance(array.isEmpty());
    }

    public LongValue length(CallContext callContext) {
        return Instances.longInstance(array.length());
    }

    public StringValue toString(CallContext callContext) {
        var sb = new StringBuilder();
        array.forEach(v -> sb.append(Instances.toString(v, callContext)));
        return Instances.stringInstance(sb.toString());
    }

}
