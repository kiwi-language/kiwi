package org.metavm.util;

import lombok.extern.slf4j.Slf4j;
import org.metavm.api.Entity;
import org.metavm.entity.IEntityContext;
import org.metavm.object.instance.core.ClassInstance;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Objects;

@Slf4j
@Entity(ephemeral = true, isNative = true)
public class MvObjectOutputStream extends ObjectOutputStream {

    public static MvObjectOutputStream create(MarkingInstanceOutput output, IEntityContext context) {
        try {
            return new MvObjectOutputStream(output, context);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final transient MarkingInstanceOutput out;
    private final transient IEntityContext context;

    public MvObjectOutputStream(MarkingInstanceOutput out, IEntityContext context) throws IOException {
        super();
        this.out = out;
        this.context = context;
    }

    @Override
    protected void writeObjectOverride(Object obj) {
        var v = Instances.fromJavaValue(obj, false, () -> context.getInstance(obj).getReference());
        out.writeValue(v);
    }

    @Override
    public void defaultWriteObject() {
        var inst = (ClassInstance) Objects.requireNonNull(out.getCurrent());
        out.enterDefaultWriting();
        inst.defaultWrite(out);
        out.exitingDefaultWriting();
    }

    @Override
    public void writeUTF(String str) {
        out.writeUTF(str);
    }

    @Override
    public void writeByte(int val) {
        out.write(val);
    }

    @Override
    public void writeShort(int val) {
        out.writeInt(val);
    }

    @Override
    public void writeInt(int val) {
        out.writeInt(val);
    }

    @Override
    public void writeLong(long val) {
        out.writeLong(val);
    }

    @Override
    public void writeFloat(float val) {
        out.writeDouble(val);
    }

    @Override
    public void writeDouble(double val) {
        out.writeDouble(val);
    }
}
