package org.metavm.object.instance.log;

import org.metavm.api.Entity;
import org.metavm.api.ValueObject;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.PhysicalId;

@Entity
public record DefaultIdentifier(
        long treeId,
        long nodeId
) implements ValueObject, Identifier {

    public Id toId() {
        return new PhysicalId(false, treeId, nodeId);
    }

}
