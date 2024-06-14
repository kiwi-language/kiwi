package org.metavm.autograph.mocks;

import org.metavm.entity.Entity;
import org.metavm.entity.EntityField;
import org.metavm.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

@EntityType(compiled = true)
public class AstProduct extends Entity {

    @EntityField(asTitle = true)
    public String title;

    public long orderCount;

    public long price;

    public long inventory;

    public AstProductState state;

    public void dec(int amount) {
        if (state != AstProductState.NORMAL || inventory < amount) {
            throw new RuntimeException("Product out of inventory of off shelf");
        }
        inventory -= amount;
    }

    public <CouponType extends AstCoupon> long calcDiscount(List<CouponType> coupons) {
        long discount = 0L;
        for (CouponType coupon : coupons) {
            discount += coupon.calc(1);
        }
        return discount;
    }

    public long calcDirectDiscount(AstDirectCoupon[] directCoupons) {
        List<AstCoupon> list = new ArrayList<>();
        //noinspection ManualArrayToCollectionCopy
        for (AstDirectCoupon directCoupon : directCoupons) {
            //noinspection UseBulkOperation
            list.add(directCoupon);
        }
        return calcDiscount(list);
    }

    public AstOrder buy(int amount, AstCoupon[] coupons) {
        dec(amount);
        List<AstCoupon> selectedCoupons = new ArrayList<>();
        long orderPrice = amount * price;
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < coupons.length; i++) {
            orderPrice -= coupons[i].use(amount);
            selectedCoupons.add(coupons[i]);
        }
        return new AstOrder(
                title + ++orderCount,
                orderPrice,
                this,
                amount,
                selectedCoupons
        );
    }

}
