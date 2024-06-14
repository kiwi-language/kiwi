package org.metavm.lab.shopping;

import org.metavm.entity.EntityType;

@EntityType(compiled = true)
public class AstDirectCoupon implements AstCoupon {

    public long discount;

    public AstCouponState state;

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
