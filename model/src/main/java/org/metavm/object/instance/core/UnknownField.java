package org.metavm.object.instance.core;

import org.metavm.util.InstanceOutput;

public class UnknownField implements IInstanceField {

    private final long recordGroupTag;
    private final int recordTag;
    private final byte[] valueBytes;

    public UnknownField(long recordGroupTag, int recordTag, byte[] valueBytes) {
        this.recordGroupTag = recordGroupTag;
        this.recordTag = recordTag;
        this.valueBytes = valueBytes;
    }

    public long getRecordGroupTag() {
        return recordGroupTag;
    }

    public int getRecordTag() {
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
        output.write(valueBytes);
    }

    @Override
    public boolean isFieldInitialized() {
        return true;
    }
}
