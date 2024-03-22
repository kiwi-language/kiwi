package tech.metavm.object.instance.core;

import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.object.type.ArrayType;
import tech.metavm.object.type.ClassType;
import tech.metavm.util.InstanceOutput;

import java.util.Objects;

public class TypePhysicalId extends PhysicalId {

    public static TypePhysicalId ofClass(long id, long nodeId) {
        return new TypePhysicalId(id, nodeId, TypeTag.CLASS);
    }

    public static TypePhysicalId of(long id, long nodeId, TypeTag typeTag) {
        return new TypePhysicalId(id, nodeId, typeTag);
    }

    private final TypeTag typeTag;

    public TypePhysicalId(long treeId, long nodeId, TypeTag typeTag) {
        super(treeId, nodeId);
        this.typeTag = typeTag;
    }

    @Override
    public Id getTypeId() {
        return switch (typeTag) {
            case CLASS -> ModelDefRegistry.getType(ClassType.class).getId();
            case ARRAY -> ModelDefRegistry.getType(ArrayType.class).getId();
        };
    }

    @Override
    public void write(InstanceOutput output) {
        output.writeIdTag(IdTag.CLASS_TYPE_PHYSICAL);
        output.writeLong(getTreeId());
        output.writeLong(getNodeId());
        output.write(typeTag.code());
    }

    public TypeTag getTypeTag() {
        return typeTag;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof TypePhysicalId that)
            return super.equals(object) && typeTag == that.typeTag;
        else
            return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), typeTag);
    }
}
