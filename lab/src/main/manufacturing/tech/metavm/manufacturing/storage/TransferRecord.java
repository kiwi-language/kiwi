package tech.metavm.manufacturing.storage;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityStruct;
import tech.metavm.manufacturing.material.Material;
import tech.metavm.manufacturing.material.Unit;

import java.util.Date;

@EntityStruct("调拨记录")
public record TransferRecord(
        @EntityField("物料") Material material,
        @EntityField("发出位置") Position from,
        @EntityField("接收位置") Position to,
        @EntityField("库存") Inventory inventory,
        @EntityField("数量") long amount,
        @EntityField("单位") Unit unit,
        @EntityField("时间") Date time
) {
}
