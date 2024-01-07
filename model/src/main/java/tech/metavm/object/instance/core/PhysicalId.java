package tech.metavm.object.instance.core;

import tech.metavm.util.InstanceOutput;

import java.util.Objects;

public final class PhysicalId extends Id {

    public static PhysicalId of(long id) {
        return new PhysicalId(id);
    }

    public static final int TAG = 1;

    private final long id;

    public PhysicalId(long id) {
        this.id = id;
    }

    @Override
    public void write(InstanceOutput output) {
        output.write(TAG);
        output.writeLong(id);
    }

    public long getId() {
        return id;
    }

    @Override
    public Long tryGetPhysicalId() {
        return id;
    }

    @Override
    public boolean isTemporary() {
        return false;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof PhysicalId that)) return false;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
