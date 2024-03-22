package tech.metavm.object.instance.core;

import tech.metavm.object.type.Type;
import tech.metavm.util.InstanceOutput;

import java.util.Objects;

public class DefaultPhysicalId extends PhysicalId {

    public static PhysicalId of(long id, long nodeId, Id typeId) {
        return new DefaultPhysicalId(id, nodeId, typeId);
    }

    public static PhysicalId of(long id, long nodeId, Type type) {
        return new DefaultPhysicalId(id, nodeId, type.getId());
    }

    private final Id typeId;

    public DefaultPhysicalId(long treeId, long nodeId, Id typeId) {
        super(treeId, nodeId);
        this.typeId = typeId;
    }

    @Override
    public Id getTypeId() {
        return typeId;
    }

    @Override
    public void write(InstanceOutput output) {
        output.writeIdTag(IdTag.DEFAULT_PHYSICAL);
        output.writeLong(getTreeId());
        output.writeLong(getNodeId());
        typeId.write(output);
    }

    @Override
    public boolean equals(Object entity) {
        if (this == entity) return true;
        if (!(entity instanceof DefaultPhysicalId that)) return false;
        if (!super.equals(entity)) return false;
        return Objects.equals(typeId, that.typeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), typeId);
    }
}
