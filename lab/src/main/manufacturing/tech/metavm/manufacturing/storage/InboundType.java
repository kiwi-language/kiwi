package tech.metavm.manufacturing.storage;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.EnumConstant;

@EntityType("入库类型")
public enum InboundType {
    @EnumConstant("数量")
    BY_AMOUNT,
    @EnumConstant("二维码")
    BY_QR_CODE,
    @EnumConstant("规格")
    BY_SPEC,
}