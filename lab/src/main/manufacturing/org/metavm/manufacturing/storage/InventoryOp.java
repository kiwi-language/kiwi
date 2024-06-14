package org.metavm.manufacturing.storage;

import org.metavm.entity.EntityType;

@EntityType
public enum InventoryOp {
    INBOUND,
    OUTBOUND,
    MOVE_INBOUND,
    MOVE_OUTBOUND,
    ADJUSTMENT,
    ATTRIBUTE_ADJUSTMENT,
}
