package tech.metavm.manufacturing.storage;

import tech.metavm.entity.EntityType;

@EntityType
public enum InventoryOp {
    INBOUND,
    OUTBOUND,
    MOVE_INBOUND,
    MOVE_OUTBOUND,
    ADJUSTMENT,
    ATTRIBUTE_ADJUSTMENT,
}
