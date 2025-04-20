package org.metavm.manufacturing.storage;

import org.metavm.api.EntityStruct;
import org.metavm.manufacturing.material.Batch;
import org.metavm.manufacturing.material.Client;
import org.metavm.manufacturing.material.Material;
import org.metavm.manufacturing.material.Supplier;

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
