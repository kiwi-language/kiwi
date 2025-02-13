package org.metavm.manufacturing.storage;

import org.metavm.api.EntityStruct;

import java.util.List;

@EntityStruct(ephemeral = true)
public record InboundReversalRequest(
        List<InboundReversalRequestItem> items
) {

}
