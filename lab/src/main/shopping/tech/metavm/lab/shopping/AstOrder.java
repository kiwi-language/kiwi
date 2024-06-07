package tech.metavm.lab.shopping;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

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
