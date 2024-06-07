package tech.metavm.autograph.mocks;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityType;

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
