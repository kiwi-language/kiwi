package org.metavm.object.instance.core;

import org.jetbrains.annotations.NotNull;
import org.metavm.util.InstanceOutput;
import org.metavm.util.MvOutput;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class UnknownField implements IInstanceField {

    private final ClassInstance owner;
    private final long recordGroupTag;
    private final int recordTag;
    private byte[] bytes;
    private @Nullable Value value;

    public UnknownField(ClassInstance owner, long recordGroupTag, int recordTag, byte[] bytes) {
        this.owner = owner;
        this.recordGroupTag = recordGroupTag;
        this.recordTag = recordTag;
        this.bytes = bytes;
    }

    public long getKlassTag() {
        return recordGroupTag;
    }

    public int getTag() {
        return recordTag;
    }

    @Override
    public boolean shouldSkipWrite() {
        return false;
    }

    @Override
    public void set(Value value) {
        var bout = new ByteArrayOutputStream();
        var out = new InstanceOutput(bout);
        out.writeValue(value);
        this.value = value;
        this.bytes = bout.toByteArray();
    }

    @Override
    public void clear() {
    }

    @Override
    public void writeValue(MvOutput output) {
        output.write(bytes);
    }

    public byte[] getBytes() {
        return bytes;
    }

    @Override
    public @NotNull Value getValue() {
        if(value == null) {
            var input = owner.getContext().createInstanceInput(new ByteArrayInputStream(bytes));
            value = input.readValue();
        }
        return value;
    }

    @Override
    public boolean isFieldInitialized() {
        return true;
    }
}
