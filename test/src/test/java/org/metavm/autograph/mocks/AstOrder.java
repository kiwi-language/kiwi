package org.metavm.autograph.mocks;

import org.metavm.entity.ChildEntity;
import org.metavm.entity.Entity;
import org.metavm.entity.EntityField;
import org.metavm.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

@EntityType(compiled = true)
public class AstOrder extends Entity {

    @EntityField(asTitle = true)
    public final String code;

    public final long price;

    public final AstProduct product;

    public final int amount;

    @ChildEntity
    public final List<AstCoupon> coupons;

    public int state;

    public AstOrder(String code, long price, AstProduct product, int amount, List<AstCoupon> coupons) {
        this.code = code;
        this.price = price;
        this.product = product;
        this.amount = amount;
        this.coupons = new ArrayList<>(coupons);
        this.state = 0;
    }

}
