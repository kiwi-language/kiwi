package tech.metavm.mocks;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IndexDef;

@EntityType("优惠券")
public class Coupon extends Entity {

    public static final IndexDef<Coupon> IDX_DISCOUNT_TYPE_DISCOUNT = new IndexDef<>(
        Coupon.class, "discountType", "discount"
    );

    @EntityField("面额")
    private double discount;
    @EntityField("折扣类型")
    private DiscountType discountType;
    @EntityField("状态")
    private CouponState state;
    @EntityField("适用商品")
    private Product product;

    public Coupon(double discount, DiscountType discountType, CouponState state, Product product) {
        this.discount = discount;
        this.discountType = discountType;
        this.state = state;
        this.product = product;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public DiscountType getDiscountType() {
        return discountType;
    }

    public void setDiscountType(DiscountType discountType) {
        this.discountType = discountType;
    }

    public CouponState getState() {
        return state;
    }

    public void setState(CouponState state) {
        this.state = state;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
