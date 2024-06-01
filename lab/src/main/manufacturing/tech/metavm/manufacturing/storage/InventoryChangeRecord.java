package tech.metavm.manufacturing.storage;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityStruct;
import tech.metavm.manufacturing.material.Batch;
import tech.metavm.manufacturing.material.Client;
import tech.metavm.manufacturing.material.Material;
import tech.metavm.manufacturing.material.Supplier;

import javax.annotation.Nullable;
import java.util.Date;

@EntityStruct("库存变更记录")
public record InventoryChangeRecord(
        @EntityField("物料") Material material,
        @EntityField("仓位") Position position,
        @EntityField("入库") boolean in,
        @EntityField("数量") long amount,
        @EntityField("结余数量") long balanceAmount,
        @EntityField("操作") InventoryOp op,
        @EntityField("时间") Date time,
        @EntityField("业务状态") InventoryBizState bizState,
        @EntityField("批次") @Nullable Batch batch,
        @EntityField("二维码") @Nullable String qrCode,
        @EntityField("供应商") @Nullable Supplier supplier,
        @EntityField("供应商批次") @Nullable String supplierBatchNo,
        @EntityField("客户") @Nullable Client client,
        @EntityField("入厂日期") @Nullable Date arrivalDate,
        @EntityField("生产日期") @Nullable Date productionDate,
        @EntityField("过期日期") @Nullable Date expirationDate
) {
}
