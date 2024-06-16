package org.metavm.manufacturing.storage;

import org.metavm.api.ChildEntity;
import org.metavm.api.ChildList;
import org.metavm.api.EntityStruct;

@EntityStruct(ephemeral = true)
public record InboundReversalRequest(
        @ChildEntity ChildList<InboundReversalRequestItem> items
) {

}
