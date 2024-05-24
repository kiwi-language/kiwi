package tech.metavm.object.instance.core;

import tech.metavm.object.type.rest.dto.TypeKey;
import tech.metavm.util.InstanceOutput;

public class TypedPhysicalId extends PhysicalId {

    private final TypeKey typeKey;

    public TypedPhysicalId(boolean isArray, long treeId, long nodeId, TypeKey typeKey) {
        super(isArray, treeId, nodeId);
        this.typeKey = typeKey;
    }

    @Override
    public void write(InstanceOutput output) {
        output.writeIdTag(IdTag.TYPED_PHYSICAL, isArray());
        output.writeLong(treeId);
        output.writeLong(nodeId);
        typeKey.write(output);
    }

    public TypeKey getTypeKey() {
        return typeKey;
    }
}
