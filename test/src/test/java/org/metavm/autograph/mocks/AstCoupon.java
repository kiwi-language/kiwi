package org.metavm.autograph.mocks;

import org.metavm.entity.EntityType;

@EntityType(compiled = true)
public interface AstCoupon {

    long use(int amount);

    long calc(int amount);

}
