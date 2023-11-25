package tech.metavm.autograph.mocks;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

import java.util.*;

@EntityType(value = "AST产品", compiled = true)
public class AstProduct extends Entity {

    @EntityField(value = "标题", asTitle = true)
    public String title;

    @EntityField("订单数量")
    public long orderCount;

    @EntityField("价格")
    public long price;

    @EntityField("库存")
    public long inventory;

    @EntityField("状态")
    public AstProductState state;

    public void dec(int amount) {
        if (state != AstProductState.NORMAL || inventory < amount) {
            throw new RuntimeException("商品库存不足或未上架");
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

    public long calcDirectDiscount(DirectAstCoupon[] directCoupons) {
        List<AstCoupon> list = new ArrayList<>();
        //noinspection ManualArrayToCollectionCopy
        for (DirectAstCoupon directCoupon : directCoupons) {
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
