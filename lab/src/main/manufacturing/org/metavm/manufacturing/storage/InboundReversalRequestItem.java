package org.metavm.manufacturing.storage;

import org.metavm.entity.EntityStruct;
import org.metavm.manufacturing.material.Unit;

@EntityStruct(ephemeral = true)
public record InboundReversalRequestItem(
        Inventory inventory,
        long amount,
        Unit unit
) {
}
