package tech.metavm.object.instance.core;

import tech.metavm.object.type.Type;
import tech.metavm.object.type.TypeDefProvider;
import tech.metavm.object.view.MappingProvider;
import tech.metavm.util.InstanceInput;
import tech.metavm.util.InstanceOutput;
import tech.metavm.util.NncUtils;

import java.util.Objects;

public class PhysicalId extends Id {

    public static PhysicalId ofObject(long id, long nodeId, Type type) {
        return new PhysicalId(id, nodeId, type.getTypeTag());
    }

    private final long treeId;
    private final long nodeId;
    private final int typeTag;

    public PhysicalId(long treeId, long nodeId, int typeTag) {
        super(typeTag > 0 && typeTag < 4);
        this.treeId = treeId;
        this.nodeId = nodeId;
        this.typeTag = typeTag;
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
    public void write(InstanceOutput output) {
        output.writeIdTag(IdTag.OBJECT_PHYSICAL, isArray());
        output.writeLong(getTreeId());
        output.writeLong(getNodeId());
        output.writeInt(typeTag);
    }

    public void writeCompact(InstanceOutput output) {
        output.writeIdTag(IdTag.OBJECT_PHYSICAL, isArray());
        output.writeLong(getNodeId());
        output.writeInt(typeTag);
    }

    @Override
    public int getTypeTag(MappingProvider mappingProvider, TypeDefProvider typeDefProvider) {
        return typeTag;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof PhysicalId that)) return false;
        return treeId == that.treeId && nodeId == that.nodeId && typeTag == that.typeTag;
    }

    @Override
    public int hashCode() {
        return Objects.hash(treeId, nodeId, typeTag);
    }

    public static PhysicalId read(InstanceInput input, long treeId) {
        var maskedTagCode = input.read();
        var tag = IdTag.fromCode(maskedTagCode & 0x7F);
        NncUtils.requireTrue(tag == IdTag.OBJECT_PHYSICAL);
        return new PhysicalId(treeId, input.readLong(), input.readInt());
    }

}
