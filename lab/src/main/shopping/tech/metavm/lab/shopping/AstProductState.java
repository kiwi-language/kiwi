package tech.metavm.lab.shopping;

import tech.metavm.entity.EntityType;

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
