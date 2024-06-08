package tech.metavm.manufacturing.storage;

public record TransferDetail(
        TransferOrderItem transferOrderItem,
        Inventory inventory,
        long amount
) {
}
