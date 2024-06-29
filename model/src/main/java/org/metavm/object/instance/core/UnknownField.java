package org.metavm.object.instance.core;

import org.metavm.util.InstanceOutput;

public class UnknownField implements IInstanceField {

    private final long recordGroupTag;
    private final long recordTag;
    private final byte[] valueBytes;

    public UnknownField(long recordGroupTag, long recordTag, byte[] valueBytes) {
        this.recordGroupTag = recordGroupTag;
        this.recordTag = recordTag;
        this.valueBytes = valueBytes;
    }

    public long getRecordGroupTag() {
        return recordGroupTag;
    }

    public long getRecordTag() {
        return recordTag;
    }

    @Override
    public boolean shouldSkipWrite() {
        return false;
    }

    @Override
    public void writeValue(InstanceOutput output) {
        output.write(valueBytes);
    }
}
