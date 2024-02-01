package tech.metavm.lab.shopping;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.EnumConstant;

@EntityType("优惠券状态")
public enum AstCouponState {
    @EnumConstant("未使用")
    UNUSED(0),
    @EnumConstant("已使用")
    USED(1);

    final int code;

    AstCouponState(int code) {
        this.code = code;
    }
}
