package org.metavm.manufacturing.storage;

public record TransferDetail(
        TransferOrderItem transferOrderItem,
        Inventory inventory,
        long amount
) {
}
