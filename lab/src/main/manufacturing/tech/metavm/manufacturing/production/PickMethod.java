package tech.metavm.manufacturing.production;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.EnumConstant;

@EntityType("领料方式")
public enum PickMethod {
    @EnumConstant("按需领料")
    ON_DEMAND,
    // 不领料
    @EnumConstant("不领料")
    NONE,
}
