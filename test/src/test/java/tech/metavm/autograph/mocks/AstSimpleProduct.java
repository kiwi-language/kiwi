package tech.metavm.autograph.mocks;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

@EntityType(value = "AST简化产品", compiled = true)
public class AstSimpleProduct extends Entity {

    @EntityField(value = "标题", asTitle = true)
    public String title;

    @EntityField("订单数量")
    public int orderCount;

    @EntityField("价格")
    public long price;

    @EntityField("库存")
    public int inventory;

    @EntityField("状态")
    public AstProductState state;

    public long calcOrderPrice(int amount, AstSimpleCoupon[] coupons) {
        long discount = 0;
        for (int i = 0; i < coupons.length; i++) {
            discount += coupons[i].discount;
        }
        return price * amount - discount;
    }

}
