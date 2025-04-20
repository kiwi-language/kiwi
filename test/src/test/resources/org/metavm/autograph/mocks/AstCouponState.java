package org.metavm.autograph.mocks;

import org.metavm.api.Entity;
import org.metavm.util.Utils;

@Entity
public enum AstCouponState {
    UNUSED(0),
    USED(1);

    final int code;

    AstCouponState(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static AstCouponState fromCode(int code) {
        return Utils.findRequired(values(), v -> v.code == code);
    }

}
