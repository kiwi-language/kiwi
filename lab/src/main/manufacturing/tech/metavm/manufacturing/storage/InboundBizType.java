package tech.metavm.manufacturing.storage;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.EnumConstant;

@EntityType("入库业务类型")
public enum InboundBizType {
    @EnumConstant("采购")
    PURCHASE,
    @EnumConstant("退货")
    RETURN,
    @EnumConstant("其他")
    OTHER
}
