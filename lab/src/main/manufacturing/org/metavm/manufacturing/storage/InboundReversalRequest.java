package org.metavm.manufacturing.storage;

import org.metavm.entity.ChildEntity;
import org.metavm.entity.ChildList;
import org.metavm.entity.EntityStruct;

@EntityStruct(ephemeral = true)
public record InboundReversalRequest(
        @ChildEntity ChildList<InboundReversalRequestItem> items
) {

}
