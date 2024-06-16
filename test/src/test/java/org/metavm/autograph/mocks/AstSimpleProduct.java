package org.metavm.autograph.mocks;

import org.metavm.entity.Entity;
import org.metavm.api.EntityField;
import org.metavm.api.EntityType;

@EntityType(compiled = true)
public class AstSimpleProduct extends Entity {

    @EntityField(asTitle = true)
    public String title;

    public int orderCount;

    public long price;

    public int inventory;

    public AstProductState state;

    public long calcOrderPrice(int amount, AstSimpleCoupon[] coupons) {
        long discount = 0;
        for (int i = 0; i < coupons.length; i++) {
            discount += coupons[i].discount;
        }
        return price * amount - discount;
    }

}
