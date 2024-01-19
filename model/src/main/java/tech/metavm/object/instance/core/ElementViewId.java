package tech.metavm.object.instance.core;

import tech.metavm.util.InstanceOutput;

import java.util.Objects;

public class ElementViewId extends PathViewId {

    public static final int TAG = 5;
    private final int index;

    public ElementViewId(ViewId parent, long mappingId, int index) {
        super(parent, mappingId);
        this.index = index;
    }

    @Override
    public void write(InstanceOutput output) {
        output.write(TAG);
        getParent().write(output);
        output.writeLong(getMappingId());
        output.writeInt(index);
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
        if (!(object instanceof ElementViewId that)) return false;
        if (!super.equals(object)) return false;
        return index == that.index;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), index);
    }
}
