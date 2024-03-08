package tech.metavm.manufacturing.storage;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.EnumConstant;

@EntityType("调拨业务类型")
public enum TransferBizType {
    @EnumConstant("领料调拨")
    MATERIAL_PICKING,
    @EnumConstant("仓储调拨")
    STORAGE,
}
