package org.metavm.autograph.mocks;

import java.util.List;

public class ContinueFoo {

    public long calcDiscount(List<AstProduct> products, List<AstDirectCoupon> coupons, int maxDiscountPerCoupon) {
        long discount = 0;
        int numAllMatchProducts = 0;
        out:
        for (AstProduct product : products) {
            for (AstDirectCoupon coupon : coupons) {
                String result;
                if (coupon.discount > maxDiscountPerCoupon) {
                    continue out;
                }
                result = "pass";
                if (coupon.product == product) {
                    discount += coupon.discount;
                }
                System.out.println(result);
            }
            numAllMatchProducts++;
        }
        return numAllMatchProducts > 1 ? discount + 10 : discount;
    }

    public int testFor(int[] a, int b) {
        for (int i = 0; i < a.length; i++) {
            if(i % 2 == 0)
                continue;
            if(a[i] == b)
                return i;
            if(i > 100)
                break;
        }
        return -1;
    }

}
