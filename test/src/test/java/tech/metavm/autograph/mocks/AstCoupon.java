package tech.metavm.autograph.mocks;

import tech.metavm.entity.EntityType;

@EntityType(compiled = true)
public interface AstCoupon {

    long use(int amount);

    long calc(int amount);

}
