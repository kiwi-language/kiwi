package org.metavm.object.instance.log;

import org.metavm.api.Entity;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.TaggedPhysicalId;

@Entity
public interface Identifier {

    static Identifier fromEntity(org.metavm.entity.Entity entity) {
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
