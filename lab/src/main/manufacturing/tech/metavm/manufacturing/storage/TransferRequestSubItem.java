package tech.metavm.manufacturing.storage;

import tech.metavm.entity.ValueStruct;
import tech.metavm.manufacturing.material.Unit;

@ValueStruct
public record TransferRequestSubItem(
        Inventory inventory,
        long amount,
        Unit unit
) {
}
