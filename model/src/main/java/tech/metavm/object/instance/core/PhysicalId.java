package tech.metavm.object.instance.core;

import tech.metavm.util.InstanceInput;
import tech.metavm.util.InstanceOutput;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.Objects;

public abstract class PhysicalId extends Id {

    private final long treeId;
    private final long nodeId;

    public PhysicalId(boolean isArray, long treeId, long nodeId) {
        super(isArray);
        this.treeId = treeId;
        this.nodeId = nodeId;
    }

    public long getTreeId() {
        return treeId;
    }

    public long getNodeId() {
        return nodeId;
    }

    @Override
    public Long tryGetTreeId() {
        return treeId;
    }

    @Override
    public boolean isTemporary() {
        return false;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof PhysicalId that)) return false;
        return treeId == that.treeId && nodeId == that.nodeId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(treeId, nodeId);
    }

    public abstract void writeWithoutTreeId(InstanceOutput output);

    public static PhysicalId read(InstanceInput input, long treeId) {
        var maskedTagCode = input.read();
        var tag = IdTag.fromCode(maskedTagCode & 0x7F);
        NncUtils.requireTrue(tag == IdTag.OBJECT_PHYSICAL);
        return new DefaultPhysicalId(input.readInt(), treeId, input.readLong());
    }

}
