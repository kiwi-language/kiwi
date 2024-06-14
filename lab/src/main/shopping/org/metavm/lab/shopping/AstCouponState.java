package org.metavm.lab.shopping;

import org.metavm.entity.EntityType;

@EntityType
public enum AstCouponState {
    UNUSED(0),
    USED(1);

    final int code;

    AstCouponState(int code) {
        this.code = code;
    }
}
