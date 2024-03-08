package tech.metavm.manufacturing.storage;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.EnumConstant;

@EntityType("调拨单状态")
public enum TransferOrderStatus {
    @EnumConstant("待下发")
    PENDING,
    @EnumConstant("已下发")
    ISSUED,
    @EnumConstant("下发失败")
    ISSUE_FAILED,
    @EnumConstant("已完成")
    FINISHED,
}
