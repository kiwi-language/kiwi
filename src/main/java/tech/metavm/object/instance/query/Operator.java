package tech.metavm.object.instance.query;

import tech.metavm.object.meta.BuiltinTypes;
import tech.metavm.object.meta.Type;

import java.util.Arrays;
import static tech.metavm.object.instance.query.OperatorTypes.*;

public enum Operator {

    // parenthesis
    LEFT_PARENTHESIS("(", 1000, PREFIX, null),
    RIGHT_PARENTHESIS(")", 1000, PREFIX, null),

    // prefix
//        TODO: 支持负数运算符
//    NEGATE("-", 1, PREFIX, null),
    NOT("!", 1, PREFIX, BuiltinTypes.getBool()),

    // Multiply and division
    MULTIPLY("*", 2, BINARY, null),
    DIVIDE("/", 2, BINARY, null),
    MOD("%", 2, BINARY, null),

    // addition and subtraction
    ADD("+", 3, BINARY, null),
    SUBTRACT("-", 3, BINARY, null),

    // relational
    GT(">", 4, BINARY, BuiltinTypes.getBool()),
    GE(">=", 4, BINARY, BuiltinTypes.getBool()),
    LT("<", 4, BINARY, BuiltinTypes.getBool()),
    LE("<=", 4, BINARY, BuiltinTypes.getBool()),

    // euqality
    EQ("=", 5, BINARY, BuiltinTypes.getBool()),
    NE("!=", 5, BINARY, BuiltinTypes.getBool()),
    STARTS_WITH("STARTS WITH", 5, BINARY, BuiltinTypes.getBool()),
    LIKE("LIKE", 5, BINARY, BuiltinTypes.getBool()),
    IN("IN", 5, BINARY, BuiltinTypes.getBool()),
    IS_NULL("IS NULL",5, POSTFIX, BuiltinTypes.getBool()),
    IS_NOT_NULL("IS NOT NULL", 5, POSTFIX, BuiltinTypes.getBool()),
    EXISTS("EXISTS", 5, PREFIX, BuiltinTypes.getBool()),
    NOT_EXISTS("NOT EXISTS", 5, PREFIX, BuiltinTypes.getBool()),

    AND("AND", 6, BINARY, BuiltinTypes.getBool()),
    OR("OR", 6, BINARY, BuiltinTypes.getBool()),

    COMMA(",", 7, BINARY, null)


    ;


    private final String op;
    private final int precedence;
    private final int type;
    private final Type resultType;

    Operator(String op, int precedence, int type, Type resultType) {
        this.op = op;
        this.precedence = precedence;
        this.type = type;
        this.resultType = resultType;
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
        return resultType;
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

    @Override
    public String toString() {
        return op;
    }
}
