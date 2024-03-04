package tech.metavm.manufacturing.storage;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.ChildList;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.manufacturing.material.*;

import javax.annotation.Nullable;
import java.util.Date;

@EntityType(value = "入库请求", ephemeral = false)
public record InboundRequest(
        @EntityField("业务类型") InboundBizType bizType,
        @EntityField("库位") Position position,
        @EntityField("入库类型") InboundType type,
        @EntityField("物料") Material material,
        @EntityField("批次") @Nullable Batch batch,
        @EntityField("供应商") @Nullable Supplier supplier,
        @EntityField("供应商批次号") @Nullable String supplierBatchNo,
        @EntityField("客户") @Nullable Client client,
        @EntityField("入厂日期") @Nullable Date arrivalDate,
        @EntityField("生产日期") @Nullable Date productionDate,
        @EntityField("过期日期") @Nullable Date expirationDate,
        @EntityField("数量") long quantity,
        @EntityField("单位") Unit unit,
        @ChildEntity("二维码") ChildList<ByQrcodeInboundRequestItem> byQrcodeItems,
        @ChildEntity("规格") ChildList<BySpecInboundRequestItem> bySpecItems
) {

}
