package org.metavm.lab.shopping;

import org.metavm.api.ChildEntity;
import org.metavm.api.EntityField;
import org.metavm.api.EntityType;

import java.util.ArrayList;
import java.util.List;

@EntityType(compiled = true)
public class AstOrder {

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
