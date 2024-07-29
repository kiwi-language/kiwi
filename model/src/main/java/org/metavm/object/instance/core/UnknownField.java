package org.metavm.object.instance.core;

import org.jetbrains.annotations.NotNull;
import org.metavm.util.InstanceOutput;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;

public class UnknownField implements IInstanceField {

    private final ClassInstance owner;
    private final long recordGroupTag;
    private final int recordTag;
    private final byte[] bytes;
    private @Nullable Instance value;

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
    public void set(Instance value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
    }

    @Override
    public void writeValue(InstanceOutput output) {
        output.write(bytes);
    }

    public byte[] getBytes() {
        return bytes;
    }

    @Override
    public @NotNull Instance getValue() {
        if(value == null) {
            var input = owner.getContext().createInstanceInput(new ByteArrayInputStream(bytes));
            value = input.readInstance();
        }
        return value;
    }

    @Override
    public boolean isFieldInitialized() {
        return true;
    }
}
