package tech.metavm.autograph.mocks;

import org.apache.el.parser.AstOr;
import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public AstOrder buy(int amount, AstCoupon[] coupons, Long[] allowedDiscounts) {
        dec(amount);
        Map<Long, AstCoupon> couponMap = new HashMap<>();
        for (AstCoupon coupon : coupons) {
            couponMap.put(coupon.calc(amount), coupon);
        }
        long orderPrice = amount * price;
        List<AstCoupon> selectedCoupons = new ArrayList<>();
        for (Long discount : allowedDiscounts) {
            var coupon = couponMap.get(discount);
            if (coupon != null) {
                orderPrice -= coupon.use(amount);
                selectedCoupons.add(coupon);
            }
        }
        return new AstOrder(
                title + ++orderCount,
                orderPrice,
                this,
                amount,
                selectedCoupons
        );
    }

    public List<AstOrder> buy(AstPair<AstProduct, AstCoupon>[] productCouponPairs) {
        var orders = new ArrayList<AstOrder>();
        for (AstPair<AstProduct, AstCoupon> productCouponPair : productCouponPairs) {
            orders.add(productCouponPair.first.buy(
                    1, new AstCoupon[]{productCouponPair.second},
                    new Long[]{productCouponPair.second.calc(1)}
            ));
        }
        return orders;
    }

}
