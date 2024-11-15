package org.metavm.object.instance.core;

import org.metavm.object.type.TypeDefProvider;
import org.metavm.util.InstanceOutput;

public class TaggedPhysicalId extends PhysicalId {

    private final int typeTag;

    public TaggedPhysicalId(long treeId, long nodeId, int typeTag) {
        super(typeTag > 0 && typeTag <= 4, treeId, nodeId);
        this.typeTag = typeTag;
    }

    @Override
    public int getTypeTag(TypeDefProvider typeDefProvider) {
        return typeTag;
    }

    public int getTypeTag() {
        return typeTag;
    }

    @Override
    public void write(InstanceOutput output) {
        output.writeIdTag(IdTag.TAGGED_PHYSICAL, isArray());
        output.writeLong(treeId);
        output.writeLong(nodeId);
        output.writeInt(typeTag);
    }

}
