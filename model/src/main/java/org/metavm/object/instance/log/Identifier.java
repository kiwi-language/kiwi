package org.metavm.object.instance.log;

import org.metavm.api.EntityType;
import org.metavm.entity.Entity;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.TaggedPhysicalId;

@EntityType
public interface Identifier {

    static Identifier fromEntity(Entity entity) {
        return fromId(entity.getId());
    }

    static Identifier fromId(Id id) {
        if(id instanceof TaggedPhysicalId taggedPhysicalId)
            return new TaggedIdentifier(id.getTreeId(), id.getNodeId(), taggedPhysicalId.getTypeTag());
        else
            return new DefaultIdentifier(id.getTreeId(), id.getNodeId());
    }

    Id toId();

    long treeId();

    long nodeId();

}
