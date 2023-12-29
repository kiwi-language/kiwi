package tech.metavm.flow;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.EnumConstant;
import tech.metavm.util.NncUtils;

@EntityType("更新操作")
public enum UpdateOp {
    @EnumConstant("设为")
    SET(1, "="),
    @EnumConstant("增加")
    INCREASE(2, "+="),
    @EnumConstant("减少")
    DECREASE(3, "-="),

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
