package tech.metavm.object.instance.core;

import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.object.type.ArrayType;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Field;
import tech.metavm.util.InstanceOutput;

import java.util.Objects;

public class TaggedPhysicalId extends PhysicalId {

    public static TaggedPhysicalId ofClass(long id, long nodeId) {
        return new TaggedPhysicalId(IdTag.CLASS_TYPE_PHYSICAL, id, nodeId);
    }

    public static TaggedPhysicalId ofArray(long id, long nodeId) {
        return new TaggedPhysicalId(IdTag.ARRAY_TYPE_PHYSICAL, id, nodeId);
    }

    public static TaggedPhysicalId ofField(long id, long nodeId) {
        return new TaggedPhysicalId(IdTag.FIELD_PHYSICAL, id, nodeId);
    }

    private final IdTag tag;

    public TaggedPhysicalId(IdTag tag, long treeId, long nodeId) {
        super(false, treeId, nodeId);
        this.tag = tag;
    }

    @Override
    public Id getTypeId() {
        return switch (tag) {
            case CLASS_TYPE_PHYSICAL -> ModelDefRegistry.getType(ClassType.class).getId();
            case ARRAY_TYPE_PHYSICAL -> ModelDefRegistry.getType(ArrayType.class).getId();
            case FIELD_PHYSICAL -> ModelDefRegistry.getType(Field.class).getId();
            default -> throw new IllegalStateException("Unexpected value: " + tag);
        };
    }

    @Override
    public void write(InstanceOutput output) {
        output.writeIdTag(tag, false);
        output.writeLong(getTreeId());
        output.writeLong(getNodeId());
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof TaggedPhysicalId that)
            return super.equals(object) && this.tag == that.tag;
        else
            return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), tag);
    }
}
