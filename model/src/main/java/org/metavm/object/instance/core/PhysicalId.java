package org.metavm.object.instance.core;

import org.metavm.object.type.TypeDefProvider;
import org.metavm.object.type.TypeOrTypeKey;
import org.metavm.object.view.MappingProvider;
import org.metavm.util.InstanceOutput;

import java.util.Objects;

public class PhysicalId extends Id {

    public static PhysicalId of(long treeId, long nodeId, TypeOrTypeKey type) {
        var typeTag = type.getTypeTag();
        if(typeTag > 0)
            return new TaggedPhysicalId(treeId, nodeId, typeTag);
        else
            return new PhysicalId(type.isArray(), treeId, nodeId);
    }

    protected final long treeId;
    protected final long nodeId;

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
    public int getTypeTag(MappingProvider mappingProvider, TypeDefProvider typeDefProvider) {
        return 0;
    }

    @Override
    public void write(InstanceOutput output) {
        output.writeIdTag(IdTag.PHYSICAL, isArray());
        output.writeLong(getTreeId());
        output.writeLong(getNodeId());
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
