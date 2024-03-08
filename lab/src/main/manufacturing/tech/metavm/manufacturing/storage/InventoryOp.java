package tech.metavm.manufacturing.storage;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.EnumConstant;

@EntityType(value = "库存操作")
public enum InventoryOp {
    @EnumConstant("入库")
    INBOUND,
    @EnumConstant("出库")
    OUTBOUND,
    @EnumConstant("转移入库")
    MOVE_INBOUND,
    @EnumConstant("转移出库")
    MOVE_OUTBOUND,
    @EnumConstant("库存调整")
    ADJUSTMENT,
    @EnumConstant("属性调整")
    ATTRIBUTE_ADJUSTMENT,
}
