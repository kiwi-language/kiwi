package tech.metavm.lab.shopping;

import tech.metavm.entity.EntityType;

@EntityType(value = "AST优惠券", compiled = true)
public interface AstCoupon {

    long use(int amount);

    long calc(int amount);

}
