package tech.metavm.manufacturing.storage;

import tech.metavm.entity.EntityStruct;
import tech.metavm.manufacturing.material.Material;
import tech.metavm.manufacturing.material.Unit;

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
