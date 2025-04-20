package org.metavm.manufacturing.storage;

import org.metavm.api.ValueStruct;
import org.metavm.manufacturing.material.Unit;

@ValueStruct
public record TransferRequestSubItem(
        Inventory inventory,
        long amount,
        Unit unit
) {
}
