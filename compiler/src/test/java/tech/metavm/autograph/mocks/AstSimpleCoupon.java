package tech.metavm.autograph.mocks;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

@EntityType(value = "AST简化优惠券", compiled = true)
public class AstSimpleCoupon {

    @EntityField("适用商品")
    AstSimpleProduct product;

    @EntityField("折扣")
    long discount;

}
