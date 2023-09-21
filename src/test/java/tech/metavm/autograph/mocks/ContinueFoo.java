package tech.metavm.autograph.mocks;

import java.util.List;

public class ContinueFoo {

    public long calcDiscount(List<AstProduct> products, List<DirectAstCoupon> coupons, int maxDiscountPerCoupon) {
        long discount = 0;
        int numAllMatchProducts = 0;
        out:
        for (AstProduct product : products) {
            for (DirectAstCoupon coupon : coupons) {
                if (coupon.discount > maxDiscountPerCoupon) {
                    continue out;
                }
                if (coupon.product == product) {
                    discount += coupon.discount;
                }
            }
            numAllMatchProducts++;
        }
        return numAllMatchProducts > 1 ? discount + 10 : discount;
    }

}
