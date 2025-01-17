package org.metavm.entity.natives;

import org.metavm.object.instance.core.*;
import org.metavm.util.Instances;
import org.metavm.util.MarkingInstanceOutput;
import org.metavm.util.MvObjectOutputStream;

import java.util.Objects;

public class MvObjectOutputStreamNative extends NativeBase {

    private final MarkingInstanceOutput out;

    public MvObjectOutputStreamNative(ClassInstance instance) {
        out = ((MvObjectOutputStream) instance).getOut();
    }

    public Value writeObject(Value obj, CallContext callContext) {
        out.writeValue(obj);
        return Instances.nullInstance();
    }

    public Value defaultWriteObject(CallContext callContext) {
        var inst = (ClassInstance) Objects.requireNonNull(out.getCurrent());
        out.enterDefaultWriting();
        inst.defaultWrite(out);
        out.exitingDefaultWriting();
        return Instances.nullInstance();
    }

    public Value writeUTF(Value str, CallContext callContext) {
        out.writeUTF(((StringValue) str).value);
        return Instances.nullInstance();
    }

    public Value writeByte(Value val, CallContext callContext) {
        out.write(((IntValue) val).value);
        return Instances.nullInstance();
    }

    public Value writeShort(Value val, CallContext callContext) {
        out.writeInt(((IntValue) val).value);
        return Instances.nullInstance();
    }

    public Value writeInt(Value val, CallContext callContext) {
        out.writeInt(((IntValue) val).value);
        return Instances.nullInstance();
    }

    public Value writeLong(Value val, CallContext callContext) {
        out.writeLong(((LongValue) val).value);
        return Instances.nullInstance();
    }

    public Value writeFloat(Value val, CallContext callContext) {
        out.writeDouble(((FloatValue) val).value);
        return Instances.nullInstance();
    }

    public Value writeDouble(Value val, CallContext callContext) {
        out.writeDouble(((DoubleValue) val).value);
        return Instances.nullInstance();
    }

}
