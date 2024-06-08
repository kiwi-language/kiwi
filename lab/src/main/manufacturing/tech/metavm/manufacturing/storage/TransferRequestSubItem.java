package tech.metavm.manufacturing.storage;

import tech.metavm.entity.EntityStruct;
import tech.metavm.manufacturing.material.Unit;

@EntityStruct(ephemeral = true)
public record TransferRequestSubItem(
        Inventory inventory,
        long amount,
        Unit unit
) {
}
