package tech.metavm.manufacturing.storage;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityStruct;
import tech.metavm.manufacturing.material.Unit;

@EntityStruct(value = "入库冲销请求项", ephemeral = true)
public record InboundReversalRequestItem(
        @EntityField("库存") Inventory inventory,
        @EntityField("数量") long amount,
        @EntityField("单位") Unit unit
) {
}
