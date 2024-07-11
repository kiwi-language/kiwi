package org.metavm.util;

import org.metavm.object.instance.core.Id;

public record ForwardingPointer(
        Id sourceId, Id targetId
) {

    public void write(InstanceOutput output) {
        output.writeLong(sourceId.getNodeId());
        output.writeLong(targetId.getNodeId());
    }

}
