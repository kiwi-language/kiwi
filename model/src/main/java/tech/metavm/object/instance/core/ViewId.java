package tech.metavm.object.instance.core;

import tech.metavm.util.InstanceOutput;

import java.util.Objects;

public class ViewId extends Id {

    public static final int TAG = 3;

    private final long mappingId;
    private final Id sourceId;

    public ViewId(long mappingId, Id sourceId) {
        this.mappingId = mappingId;
        this.sourceId = sourceId;
    }

    @Override
    public void write(InstanceOutput output) {
        output.write(TAG);
        output.writeLong(mappingId);
        sourceId.write(output);
    }

    public Id getSourceId() {
        return sourceId;
    }

    public long getMappingId() {
        return mappingId;
    }

    @Override
    public Long tryGetPhysicalId() {
        return sourceId.tryGetPhysicalId();
    }

    @Override
    public boolean isTemporary() {
        return sourceId.isTemporary();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof ViewId viewId)) return false;
        return mappingId == viewId.mappingId && Objects.equals(sourceId, viewId.sourceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mappingId, sourceId);
    }
}
