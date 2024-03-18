package tech.metavm.lab.shopping;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

@EntityType(value = "AST立减优惠券", compiled = true)
public class AstDirectCoupon implements AstCoupon {

    @EntityField("额度")
    public long discount;

    @EntityField("状态")
    public AstCouponState state;

    @EntityField("商品")
    public AstProduct product;

    public AstDirectCoupon(long discount, AstProduct product) {
        this.discount = discount;
        this.product = product;
        state = AstCouponState.UNUSED;
    }

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
