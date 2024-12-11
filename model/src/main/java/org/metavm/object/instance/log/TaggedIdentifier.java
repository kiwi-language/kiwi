package org.metavm.object.instance.log;

import org.metavm.api.Entity;
import org.metavm.api.ValueObject;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.TaggedPhysicalId;

@Entity
public record TaggedIdentifier(
        long treeId,
        long nodeId,
        int typeTag
) implements ValueObject, Identifier {

    public Id toId() {
        return new TaggedPhysicalId(treeId, nodeId, typeTag);
    }

}
