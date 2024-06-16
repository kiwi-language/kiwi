package org.metavm.manufacturing.storage;

import org.metavm.api.EntityStruct;
import org.metavm.manufacturing.material.Material;
import org.metavm.manufacturing.material.Unit;

import java.util.Date;

@EntityStruct
public record TransferRecord(
        Material material,
        Position from,
        Position to,
        Inventory inventory,
        long amount,
        Unit unit,
        Date time
) {
}
