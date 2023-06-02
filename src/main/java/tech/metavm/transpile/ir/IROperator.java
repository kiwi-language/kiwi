package tech.metavm.transpile.ir;

import tech.metavm.transpile.IRTypeUtil;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;

public enum IROperator {

    ADD("+"),
    SUBTRACT("-"),
    MULTIPLY("*"),
    DIVIDE("/"),

    ASSIGN("="),
    GT(">", boolean.class),
    GE(">=", boolean.class),
    LT("<", boolean.class),
    LE("<=", boolean.class),
    EQ("==", boolean.class),
    NE("!=", boolean.class),

    LEFT_SHIFT("<<"),
    UNSIGNED_RIGHT_SHIFT(">>>"),
    RIGHT_SHIFT(">>");

    private final String code;
    @Nullable
    private final IRType type;

    IROperator(String code) {
        this(code, null);
    }

    IROperator(String code, @Nullable Class<?> klass) {
        this.code = code;
        this.type = klass != null ? IRTypeUtil.fromType(klass) : null;
    }

    public String getCode() {
        return code;
    }

    public static IROperator getByCode(String code) {
        return NncUtils.findRequired(values(), v -> v.code.equals(code));
    }

    public IRType getType() {
        return type;
    }
}
