package org.metavm.entity.natives;

import org.metavm.object.instance.core.*;
import org.metavm.object.type.PrimitiveType;
import org.metavm.util.Instances;

public class StringBuilderNative implements CharSequenceNative {

    private final ClassInstance instance;
    private StringBuilder sb;

    public StringBuilderNative(ClassInstance instance) {
        this.instance = instance;
    }

    public Value StringBuilder(CallContext callContext) {
        sb = new StringBuilder();
        return instance.getReference();
    }

    public Value StringBuilder__String(Value value, CallContext callContext) {
        sb = new StringBuilder(Instances.toString(value, callContext));
        return instance.getReference();
    }

    public Value StringBuilder() {
        return instance.getReference();
    }

    public Value append__char(Value value, CallContext callContext) {
        var i = (IntValue) value;
        sb.append((char) i.value);
        return instance.getReference();
    }

    public Value append__boolean(Value value, CallContext callContext) {
        var i = (IntValue) value;
        sb.append(i.value == 1);
        return instance.getReference();
    }

    public Value append__int(Value value, CallContext callContext) {
        var i = (IntValue) value;
        sb.append(i.value);
        return instance.getReference();
    }

    public Value append__long(Value value, CallContext callContext) {
        var i = (LongValue) value;
        sb.append(i.value);
        return instance.getReference();
    }

    public Value append__float(Value value, CallContext callContext) {
        var i = (FloatValue) value;
        sb.append(i.value);
        return instance.getReference();
    }

    public Value append__double(Value value, CallContext callContext) {
        var i = (DoubleValue) value;
        sb.append(i.value);
        return instance.getReference();
    }

    public Value append__String(Value value, CallContext callContext) {
        var i = (StringReference) value;
        sb.append(i.getValue());
        return instance.getReference();
    }

    public Value append__Any(Value value, CallContext callContext) {
        sb.append(Instances.toString(value, callContext));
        return instance.getReference();
    }

    @Override
    public Value isEmpty(CallContext callContext) {
        return Instances.intInstance(sb.isEmpty());
    }

    @Override
    public Value length(CallContext callContext) {
        return Instances.intInstance(sb.length());
    }

    @Override
    public Value charAt(Value index, CallContext callContext) {
        var i = ((IntValue) index).value;
        return Instances.intInstance(sb.charAt(i));
    }

    @Override
    public Value toString(CallContext callContext) {
        return Instances.stringInstance(sb.toString());
    }

}
