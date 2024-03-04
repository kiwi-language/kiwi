package tech.metavm.manufacturing.storage;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.EnumConstant;

@EntityType("入库单状态")
public enum InboundOrderState {
    @EnumConstant("新建")
    NEW,
    @EnumConstant("已下发")
    ISSUED,
    @EnumConstant("已入库")
    INBOUND,
    @EnumConstant("已取消")
    CANCELLED
}
