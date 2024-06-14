package org.metavm.autograph.mocks;

import java.util.List;

public class LivenessFoo {

    public long test(List<AstDirectCoupon> coupons, long maxDiscountPerCoupon) {
        long totalDiscount = 0;
        boolean break1 = false;
        for (AstDirectCoupon coupon : coupons) {
            __extraLoopTest__(break1);
            if(coupon.discount > maxDiscountPerCoupon) {
                break1 = true;
            }
            totalDiscount += coupon.discount;
        }
        return totalDiscount;
    }

    private void __extraLoopTest__(boolean condition) {}

}
