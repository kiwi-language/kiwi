package tech.metavm.object.instance.core;

import tech.metavm.util.InstanceOutput;

import java.util.Objects;

public class FieldViewId extends PathViewId {

    public static final int TAG = 6;
    public final long fieldId;

    public FieldViewId(ViewId parent, long mappingId, long fieldId) {
        super(parent, mappingId);
        this.fieldId = fieldId;
    }

    @Override
    public void write(InstanceOutput output) {
        output.write(TAG);
        getParent().write(output);
        output.writeLong(getMappingId());
        output.writeLong(fieldId);
    }

    @Override
    public Long tryGetPhysicalId() {
        return null;
    }

    @Override
    public boolean isTemporary() {
        return getParent().isTemporary();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof FieldViewId that)) return false;
        if (!super.equals(object)) return false;
        return fieldId == that.fieldId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fieldId);
    }
}
