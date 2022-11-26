package tech.metavm.expression;

import tech.metavm.object.instance.query.Operator;
import tech.metavm.util.NncUtils;

public enum ConditionOpCode {

    EQ(1, Operator.EQ),
    NE(2, Operator.NE),
    GT(3, Operator.GT),
    GE(4, Operator.GE),
    LT(5, Operator.LT),
    LE(6, Operator.LE),
    IS_NULL(7, Operator.IS_NULL),
    IS_NOT_NULL(8, Operator.IS_NOT_NULL)

    ;

    private final int code;
    private final Operator operator;


    ConditionOpCode(int code, Operator operator) {
        this.code = code;
        this.operator = operator;
    }

    public static ConditionOpCode getByOperator(Operator operator) {
        return NncUtils.findRequired(values(), v -> v.operator == operator);
    }

    public int code() {
        return code;
    }
}
