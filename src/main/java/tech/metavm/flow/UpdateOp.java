package tech.metavm.flow;

import tech.metavm.util.NncUtils;

public enum UpdateOp {

    SET(1),
    INCREASE(2),
    DECREASE(3),

    ;

    private final int code;

    UpdateOp(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static UpdateOp getByCode(int code) {
        return NncUtils.findRequired(values(), v -> v.code == code);
    }

}
