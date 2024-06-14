package org.metavm.autograph.mocks;

import org.metavm.entity.Entity;
import org.metavm.entity.EntityType;

@EntityType(compiled = true)
public class AstDirectCoupon extends Entity implements AstCoupon {

    public long discount;

    public AstCouponState state;

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
