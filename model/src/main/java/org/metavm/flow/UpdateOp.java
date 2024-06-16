package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.util.NncUtils;

@EntityType
public enum UpdateOp {
    SET(1, "="),
    INC(2, "+="),
    DEC(3, "-="),

    ;

    private final int code;

    private final String op;

    UpdateOp(int code, String op) {
        this.code = code;
        this.op = op;
    }

    public int code() {
        return code;
    }

    public String op() {
        return op;
    }

    public static UpdateOp getByCode(int code) {
        return NncUtils.findRequired(values(), v -> v.code == code);
    }

}
