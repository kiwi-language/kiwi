package tech.metavm.manufacturing.storage;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.EnumConstant;

@EntityType("库存业务状态")
public enum InventoryBizState {
    @EnumConstant("初始")
    INITIAL,
    @EnumConstant("质检中")
    INSPECTING,
}
