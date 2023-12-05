package tech.metavm.autograph.mocks;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

@EntityType(value = "AST立减优惠券", compiled = true)
public class DirectAstCoupon extends Entity implements AstCoupon {

    @EntityField(value = "额度", asTitle = true)
    public long discount;

    @EntityField("状态")
    public AstCouponState state;

    @EntityField("商品")
    public AstProduct product;

    public long use(int amount) {
        if(state != AstCouponState.UNUSED) {
            throw new RuntimeException("The coupon is already used");
        }
        state = AstCouponState.USED;
        return discount;
    }

    @Override
    public long calc(int amount) {
        return discount;
    }

}
