package org.metavm.lab.shopping;

import org.metavm.api.Entity;

@Entity
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
