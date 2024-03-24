package tech.metavm.object.instance.core;

import tech.metavm.object.type.Type;
import tech.metavm.util.InstanceOutput;

import java.util.Objects;

public class DefaultPhysicalId extends PhysicalId {

    public static PhysicalId ofObject(long id, long nodeId, Id typeId) {
        return new DefaultPhysicalId(false, id, nodeId, typeId);
    }

    public static PhysicalId ofObject(long id, long nodeId, Type type) {
        return new DefaultPhysicalId(false, id, nodeId, type.getId());
    }

    public static PhysicalId ofArray(long id, long nodeId, Id typeId) {
        return new DefaultPhysicalId(true, id, nodeId, typeId);
    }

    private final Id typeId;

    public DefaultPhysicalId(boolean isArray, long treeId, long nodeId, Id typeId) {
        super(isArray, treeId, nodeId);
        this.typeId = typeId;
    }

    @Override
    public Id getTypeId() {
        return typeId;
    }

    @Override
    public void write(InstanceOutput output) {
        output.writeIdTag(IdTag.OBJECT_PHYSICAL, isArray());
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
