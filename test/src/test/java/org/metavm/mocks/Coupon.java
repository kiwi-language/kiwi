package org.metavm.mocks;

import org.metavm.entity.Entity;
import org.metavm.entity.EntityType;
import org.metavm.entity.IndexDef;

@EntityType
public class Coupon extends Entity {

    public static final IndexDef<Coupon> IDX_DISCOUNT_TYPE_DISCOUNT = new IndexDef<>(
        Coupon.class, false, "discountType", "discount"
    );

    private double discount;
    private DiscountType discountType;
    private CouponState state;
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
