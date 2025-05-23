package org.metavm.lab.shopping;

import org.metavm.api.EntityField;
import org.metavm.api.Entity;

import java.util.ArrayList;
import java.util.List;

@Entity(compiled = true)
public class AstProduct {

    @EntityField(asTitle = true)
    public String title;

    public long orderCount;

    public long price;

    public long inventory;

    public AstProductState state;

    public AstProduct(String title, long price, long inventory) {
        this.title = title;
        this.price = price;
        this.inventory = inventory;
        state = AstProductState.NORMAL;
    }

    public void dec(int amount) {
        if (state != AstProductState.NORMAL || inventory < amount) {
            throw new RuntimeException("Production is out of inventory or off shelf");
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
