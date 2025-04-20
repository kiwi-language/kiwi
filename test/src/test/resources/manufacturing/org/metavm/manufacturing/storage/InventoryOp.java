package org.metavm.manufacturing.storage;

import org.metavm.api.Entity;

@Entity
public enum InventoryOp {
    INBOUND,
    OUTBOUND,
    MOVE_INBOUND,
    MOVE_OUTBOUND,
    ADJUSTMENT,
    ATTRIBUTE_ADJUSTMENT,
}
