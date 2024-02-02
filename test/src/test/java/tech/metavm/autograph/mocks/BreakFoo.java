package tech.metavm.autograph.mocks;

import java.util.List;

public class BreakFoo {

    public long testForeach(List<AstProduct> products, List<AstDirectCoupon> coupons, int maxDiscountPerCoupon) {
        long totalDiscount = 0;
        out:
        for (AstProduct product : products) {
            for (AstDirectCoupon coupon : coupons) {
                if (coupon.discount > maxDiscountPerCoupon) {
                    break;
                }
                if (coupon.state == AstCouponState.USED) {
                    break out;
                }
                if (coupon.product == product) {
                    totalDiscount += coupon.discount;
                }
            }
        }
        return totalDiscount;
    }

    public int testWhile(AstProduct product, AstDirectCoupon coupon) {
        int amount = 0;
        while (amount * product.price <= coupon.discount) {
            amount++;
            if (amount > 10) {
                break;
            }
        }
        return amount;
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    public long testFor(AstProduct[] products, AstDirectCoupon[] coupons, int maxDiscountPerCoupon) {
        long totalDiscount = 0;
        boolean error = false;
        out:
        for (int i = 0; i < products.length; i++) {
            for (int j = 0; j < coupons.length; j++) {
                if (coupons[j].state != AstCouponState.UNUSED) {
                    error = true;
                    break out;
                }
                if (coupons[j].discount > maxDiscountPerCoupon) {
                    break;
                }
                if (coupons[j].product == products[i]) {
                    totalDiscount += coupons[j].discount;
                }
            }
        }
        return error ? -1 : totalDiscount;
    }

}