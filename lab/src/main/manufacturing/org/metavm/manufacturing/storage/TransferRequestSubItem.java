package org.metavm.manufacturing.storage;

import org.metavm.entity.ValueStruct;
import org.metavm.manufacturing.material.Unit;

@ValueStruct
public record TransferRequestSubItem(
        Inventory inventory,
        long amount,
        Unit unit
) {
}
