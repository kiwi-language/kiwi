package tech.metavm.entity;

import java.util.Objects;
import java.util.function.BiPredicate;

public enum IndexQueryOperator {

    EQ(1, "=", Objects::equals),
    GT(2, ">", (i1,i2) -> i1 > i2),
    GE(3, ">=", (i1,i2) -> i1 >= i2),
    LT(4, "<", (i1,i2) -> i1 < i2),
    LE(5, "<=", (i1,i2) -> i1 <= i2)

    ;

    private final int code;
    private final String op;
    private final BiPredicate<Long, Long> func;

    IndexQueryOperator(int code, String op,  BiPredicate<Long, Long> func) {
        this.code = code;
        this.op = op;
        this.func = func;
    }

    public int code() {
        return code;
    }

    public String op() {
        return op;
    }

    public boolean evaluate(long operand1, long operand2) {
        return func.test(operand1, operand2);
    }

}
