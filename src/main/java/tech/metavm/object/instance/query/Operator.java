package tech.metavm.object.instance.query;

import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;

import java.util.Arrays;

import static tech.metavm.object.instance.query.OperatorTypes.*;

public enum Operator {
    // prefix
//        TODO: 支持负数运算符
//    NEGATE("-", 1, PREFIX, null),
    NOT(1, "!", 1, PREFIX, Boolean.class),

    // Multiply and division
    MULTIPLY(2, "*", 2, BINARY, null),
    DIVIDE(3, "/", 2, BINARY, null),
    MOD(4, "%", 2, BINARY, null),

    // addition and subtraction
    ADD(5, "+", 3, BINARY, null),
    SUBTRACT(6, "-", 3, BINARY, null),

    // relational
    GT(7, ">", 4, BINARY, Boolean.class),
    GE(8, ">=", 4, BINARY, Boolean.class),
    LT(9, "<", 4, BINARY, Boolean.class),
    LE(10, "<=", 4, BINARY, Boolean.class),

    // euqality
    EQ(11, "=", 5, BINARY, Boolean.class),
    NE(12, "!=", 5, BINARY, Boolean.class),
    STARTS_WITH(13, "STARTS WITH", 5, BINARY, Boolean.class),
    LIKE(14, "LIKE", 5, BINARY, Boolean.class),
    IN(15, "IN", 5, BINARY, Boolean.class),
    IS_NULL(16, "IS NULL",5, POSTFIX, Boolean.class),
    IS_NOT_NULL(17, "IS NOT NULL", 5, POSTFIX, Boolean.class),
    EXISTS(18, "EXISTS", 5, PREFIX, Boolean.class),
    NOT_EXISTS(19, "NOT EXISTS", 5, PREFIX, Boolean.class),

    AND(20, "AND", 6, BINARY, Boolean.class),
    OR(21, "OR", 7, BINARY, Boolean.class),

    COMMA(22, ",", 8, BINARY, null),


    // parenthesis
    LEFT_PARENTHESIS(23, "(", 1000, PREFIX, null),
    RIGHT_PARENTHESIS(24, ")", 1000, PREFIX, null),
    LEFT_BRACKET(25, "[", 1000, PREFIX, null),
    RIGHT_BRACKET(26, "]", 1000, PREFIX, null),

    ;


    private final int code;
    private final String op;
    private final int precedence;
    private final int type;
    private final Class<?> javaType;

    Operator(int code, String op, int precedence, int type, Class<?> javaType) {
        this.code = code;
        this.op = op;
        this.precedence = precedence;
        this.type = type;
        this.javaType = javaType;
    }

    public static Operator getByCode(int code) {
        return NncUtils.findRequired(values(), op -> op.code == code);
    }

    public static Operator getByOpRequired(String op) {
        return Arrays.stream(values())
                .filter(operator -> operator.op.equalsIgnoreCase(op))
                .findAny()
                .orElseThrow(() -> new RuntimeException("Invalid operator '" + op + "'"));
    }

    public static boolean isOperator(String op) {
        return Arrays.stream(values()).anyMatch(operator -> operator.op.equalsIgnoreCase(op));
    }

    public Type resultType() {
        return NncUtils.get(javaType, ModelDefRegistry::getType);
    }

    public int precedence() {
        return precedence;
    }

    public boolean isPostfix() {
        return type == POSTFIX;
    }

    public boolean isPrefix() {
        return type == PREFIX;
    }

    public boolean isUnary() {
        return isPrefix() || isPostfix();
    }

    public boolean isBinary() {
        return type == BINARY;
    }

    public int code() {
        return this.code;
    }

    @Override
    public String toString() {
        return op;
    }
}
