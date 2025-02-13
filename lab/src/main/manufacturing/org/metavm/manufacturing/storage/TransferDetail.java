package org.metavm.manufacturing.storage;

public record TransferDetail(
        TransferOrder.Item transferOrderItem,
        Inventory inventory,
        long amount
) {
}
