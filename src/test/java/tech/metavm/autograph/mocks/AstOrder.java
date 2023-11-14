package tech.metavm.autograph.mocks;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.EntityField;
import tech.metavm.util.InstaUtils;

import java.util.ArrayList;
import java.util.List;

@EntityType(value = "AST订单", compiled = true)
public class AstOrder extends Entity {

    @EntityField(value = "编号", asTitle = true)
    public final String code;

    @EntityField("价格")
    public final long price;

    @EntityField("商品")
    public final AstProduct product;

    @EntityField("数量")
    public final int amount;

    @ChildEntity("优惠券")
    public final List<AstCoupon> coupons;

    @EntityField("状态")
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
