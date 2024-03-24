package tech.metavm.object.instance.core;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
    public Long tryGetPhysicalId() {
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

    @JsonIgnore
    public abstract Id getTypeId();

}
