package tech.metavm.object.instance.query;

import java.util.Arrays;
import static tech.metavm.object.instance.query.OperatorTypes.*;

public enum Operator {

    // parenthesis
    LEFT_PARENTHESIS("(", 1000, PREFIX),
    RIGHT_PARENTHESIS(")", 1000, PREFIX),

    // prefix
    NEGATE("-", 1, PREFIX),
    NOT("!", 1, PREFIX),

    // Multiply and division
    MULTIPLY("*", 2, BINARY),
    DIVIDE("/", 2, BINARY),
    MOD("%", 2, BINARY),

    // addition and subtraction
    ADD("+", 3, BINARY),
    SUBTRACT("-", 3, BINARY),

    // relational
    GT(">", 4, BINARY),
    GE(">=", 4, BINARY),
    LT("<", 4, BINARY),
    LE("<=", 4, BINARY),

    // euqality
    EQ("=", 5, BINARY),
    NE("!=", 5, BINARY),
    STARTS_WITH("STARTS WITH", 5, BINARY),
    LIKE("LIKE", 5, BINARY),
    IN("IN", 5, BINARY),
    IS_NULL("IS NULL",5, POSTFIX),
    IS_NOT_NULL("IS NOT NULL", 5, POSTFIX),
    EXISTS("EXISTS", 5, PREFIX),
    NOT_EXISTS("NOT EXISTS", 5, PREFIX),

    AND("AND", 6, BINARY),
    OR("OR", 6, BINARY),

    COMMA(",", 7, BINARY)


    ;


    private final String op;
    private final int precedence;
    private final int type;

    Operator(String op, int precedence, int type) {
        this.op = op;
        this.precedence = precedence;
        this.type = type;
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
