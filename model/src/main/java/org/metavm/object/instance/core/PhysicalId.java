package org.metavm.object.instance.core;

import org.metavm.object.type.TypeDefProvider;
import org.metavm.util.MvOutput;

import java.util.Objects;

public class PhysicalId extends Id {

    public static PhysicalId of(long treeId, long nodeId) {
        return new PhysicalId(treeId, nodeId);
    }

    protected final long treeId;
    protected final long nodeId;

    public PhysicalId(long treeId, long nodeId) {
        super();
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
    public int getTypeTag(TypeDefProvider typeDefProvider) {
        return 0;
    }

    @Override
    public int compareTo0(Id id) {
        var that = (PhysicalId) id;
        var r = Long.compare(treeId, that.treeId);
        if (r != 0)
            return r;
        return Long.compare(nodeId, that.nodeId);
    }

    @Override
    public void write(MvOutput output) {
        output.write(IdTag.PHYSICAL.code());
        output.writeLong(getTreeId());
        output.writeLong(getNodeId());
    }

    @Override
    public boolean isRoot() {
        return nodeId == 0L;
    }

    @Override
    public int getTag() {
        return 2;
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

}
