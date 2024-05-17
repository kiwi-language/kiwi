package tech.metavm.object.instance.core;

import tech.metavm.object.type.Type;
import tech.metavm.object.type.TypeDefProvider;
import tech.metavm.object.type.TypeTags;
import tech.metavm.object.type.rest.dto.TypeKey;
import tech.metavm.object.view.MappingProvider;
import tech.metavm.util.InstanceOutput;

import java.util.Objects;

public class DefaultPhysicalId extends PhysicalId {

    public static PhysicalId ofObject(long id, long nodeId, Type type) {
        return new DefaultPhysicalId(type.getTypeTag(), id, nodeId);
    }

    private final int typeTag;

    public DefaultPhysicalId(int typeTag, long treeId, long nodeId) {
        super(false, treeId, nodeId);
        this.typeTag = typeTag;
    }

    @Override
    public void write(InstanceOutput output) {
        output.writeIdTag(IdTag.OBJECT_PHYSICAL, isArray());
        output.writeLong(getTreeId());
        output.writeLong(getNodeId());
        output.writeInt(typeTag);
    }

    @Override
    public void writeWithoutTreeId(InstanceOutput output) {
        output.writeIdTag(IdTag.OBJECT_PHYSICAL, isArray());
        output.writeLong(getNodeId());
        output.writeInt(typeTag);
    }

    @Override
    public int getTypeTag(MappingProvider mappingProvider, TypeDefProvider typeDefProvider) {
        return typeTag;
    }

    @Override
    public boolean equals(Object entity) {
        if (this == entity) return true;
        if (!(entity instanceof DefaultPhysicalId that)) return false;
        if (!super.equals(entity)) return false;
        return typeTag == that.typeTag;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), typeTag);
    }
}
