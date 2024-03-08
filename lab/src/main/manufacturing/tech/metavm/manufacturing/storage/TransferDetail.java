package tech.metavm.manufacturing.storage;

import tech.metavm.entity.EntityField;

public record TransferDetail(
        @EntityField("调拨单行") TransferOrderItem transferOrderItem,
        @EntityField("库存") Inventory inventory,
        @EntityField("数量") long amount
) {
}
