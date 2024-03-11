package tech.metavm.manufacturing.production;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.EnumConstant;

@EntityType("投料方式")
public enum FeedingType {
    // 申请投料
    @EnumConstant("申请投料")
    APPLY,
    // 直接投料
    @EnumConstant("直接投料")
    DIRECT,

}
