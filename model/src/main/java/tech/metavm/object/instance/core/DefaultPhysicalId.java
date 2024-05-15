package tech.metavm.object.instance.core;

import tech.metavm.object.type.Type;
import tech.metavm.object.type.rest.dto.TypeKey;
import tech.metavm.util.InstanceOutput;

import java.util.Objects;

public class DefaultPhysicalId extends PhysicalId {

    public static PhysicalId ofObject(long id, long nodeId, TypeKey typeKey) {
        return new DefaultPhysicalId(false, id, nodeId, typeKey);
    }

    public static PhysicalId ofObject(long id, long nodeId, Type type) {
        return new DefaultPhysicalId(false, id, nodeId, type.toTypeKey());
    }

    public static PhysicalId ofArray(long id, long nodeId, Type type) {
        return new DefaultPhysicalId(true, id, nodeId, type.toTypeKey());
    }

    private final TypeKey typeKey;

    public DefaultPhysicalId(boolean isArray, long treeId, long nodeId, TypeKey typeKey) {
        super(isArray, treeId, nodeId);
        this.typeKey = typeKey;
    }

    @Override
    public TypeKey getTypeKey() {
        return typeKey;
    }

    @Override
    public void write(InstanceOutput output) {
        output.writeIdTag(IdTag.OBJECT_PHYSICAL, isArray());
        output.writeLong(getTreeId());
        output.writeLong(getNodeId());
        typeKey.write(output);
    }

    @Override
    public void writeWithoutTreeId(InstanceOutput output) {
        output.writeIdTag(IdTag.OBJECT_PHYSICAL, isArray());
        output.writeLong(getNodeId());
        typeKey.write(output);
    }

    @Override
    public boolean equals(Object entity) {
        if (this == entity) return true;
        if (!(entity instanceof DefaultPhysicalId that)) return false;
        if (!super.equals(entity)) return false;
        return Objects.equals(typeKey, that.typeKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), typeKey);
    }
}
