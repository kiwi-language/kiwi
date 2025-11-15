package org.metavm.util;

import lombok.extern.slf4j.Slf4j;
import org.metavm.api.Entity;
import org.metavm.entity.natives.CallContext;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.*;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

@Slf4j
@Entity(ephemeral = true)
public class MvObjectOutputStream implements NativeEphemeralObject {

    public static MvObjectOutputStream create(MarkingInstanceOutput output) {
        try {
            return new MvObjectOutputStream(output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final transient MarkingInstanceOutput out;
    private final transient InstanceState state = InstanceState.ephemeral(this);

    public MvObjectOutputStream(MarkingInstanceOutput out) throws IOException {
        super();
        this.out = out;
    }

    public MarkingInstanceOutput getOut() {
        return out;
    }

    @Override
    public InstanceState state() {
        return state;
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
        out.writeUTF((Instances.toJavaString(str)));
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


    @Override
    public void forEachReference(Consumer<Reference> action) {
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
    }
}
