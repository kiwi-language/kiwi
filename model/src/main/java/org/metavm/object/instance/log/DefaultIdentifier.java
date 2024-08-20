package org.metavm.object.instance.log;

import org.metavm.api.EntityType;
import org.metavm.api.ValueObject;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.PhysicalId;

@EntityType
public record DefaultIdentifier(
        long treeId,
        long nodeId
) implements ValueObject, Identifier {

    public Id toId() {
        return new PhysicalId(false, treeId, nodeId);
    }

}
