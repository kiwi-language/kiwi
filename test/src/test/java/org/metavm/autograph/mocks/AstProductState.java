package org.metavm.autograph.mocks;

import org.metavm.api.EntityType;

@EntityType
public enum AstProductState {

    NORMAL(0),

    OFF_THE_SHELF(1)
    ;

    private final int code;

    AstProductState(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
