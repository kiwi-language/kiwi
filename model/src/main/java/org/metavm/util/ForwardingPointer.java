package org.metavm.util;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.TreeTags;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Message;

public record ForwardingPointer(
        Id sourceId, Id targetId
) implements Comparable<ForwardingPointer>, Message {

    public void write(InstanceOutput output) {
        output.writeLong(sourceId.getNodeId());
        output.writeLong(targetId.getNodeId());
    }

    @Override
    public int compareTo(@NotNull ForwardingPointer o) {
        var cmp = sourceId.compareTo(o.sourceId);
        if(cmp != 0)
            return cmp;
        return targetId.compareTo(o.targetId);
    }

    @Override
    public void writeTo(MvOutput output) {
        output.write(TreeTags.RELOCATED);
        output.writeId(sourceId);
        output.writeId(targetId);
    }
}
