package tech.metavm.manufacturing.storage;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.manufacturing.material.*;

import javax.annotation.Nullable;
import java.util.Date;

@EntityType
public record InboundRecord(
        @EntityField(value = "入库单编码", asTitle = true) String inboundOrderCode,
        @EntityField("入库单项") InboundOrderItem inboundOrderItem,
        @EntityField("业务类型") InboundBizType bizType,
        @EntityField("物料") Material material,
        @EntityField("库位") Position position,
        @EntityField("数量") long quantity,
        @EntityField("单位") Unit unit,
        @EntityField("二维码") @Nullable String qrCode,
        @EntityField("批次") @Nullable Batch batch,
        @EntityField("供应商") @Nullable Supplier supplier,
        @EntityField("供应商批次号") @Nullable String supplierBatchNo,
        @EntityField("客户") @Nullable Client client,
        @EntityField("入厂日期") @Nullable Date arrivalDate,
        @EntityField("生产日期") @Nullable Date productionDate,
        @EntityField("过期日期") @Nullable Date expirationDate,
        @EntityField("创建时间") Date createdAt
) {

}
