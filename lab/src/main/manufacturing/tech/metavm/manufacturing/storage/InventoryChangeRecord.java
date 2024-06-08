package tech.metavm.manufacturing.storage;

import tech.metavm.entity.EntityStruct;
import tech.metavm.manufacturing.material.Batch;
import tech.metavm.manufacturing.material.Client;
import tech.metavm.manufacturing.material.Material;
import tech.metavm.manufacturing.material.Supplier;

import javax.annotation.Nullable;
import java.util.Date;

@EntityStruct
public record InventoryChangeRecord(
        Material material,
        Position position,
        boolean in,
        long amount,
        long balanceAmount,
        InventoryOp op,
        Date time,
        InventoryBizState bizState,
        @Nullable Batch batch,
        @Nullable String qrCode,
        @Nullable Supplier supplier,
        @Nullable String supplierBatchNo,
        @Nullable Client client,
        @Nullable Date arrivalDate,
        @Nullable Date productionDate,
        @Nullable Date expirationDate
) {
}
