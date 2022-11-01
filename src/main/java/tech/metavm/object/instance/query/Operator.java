package tech.metavm.object.instance.query;

import tech.metavm.entity.EntityContext;
import tech.metavm.object.meta.StdTypeConstants;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;

import java.util.Arrays;

import static tech.metavm.object.instance.query.OperatorTypes.*;

public enum Operator {

    // parenthesis
    LEFT_PARENTHESIS("(", 1000, PREFIX, null),
    RIGHT_PARENTHESIS(")", 1000, PREFIX, null),

    // prefix
//        TODO: 支持负数运算符
//    NEGATE("-", 1, PREFIX, null),
    NOT("!", 1, PREFIX, StdTypeConstants.BOOL),

    // Multiply and division
    MULTIPLY("*", 2, BINARY, null),
    DIVIDE("/", 2, BINARY, null),
    MOD("%", 2, BINARY, null),

    // addition and subtraction
    ADD("+", 3, BINARY, null),
    SUBTRACT("-", 3, BINARY, null),

    // relational
    GT(">", 4, BINARY, StdTypeConstants.BOOL),
    GE(">=", 4, BINARY, StdTypeConstants.BOOL),
    LT("<", 4, BINARY, StdTypeConstants.BOOL),
    LE("<=", 4, BINARY, StdTypeConstants.BOOL),

    // euqality
    EQ("=", 5, BINARY, StdTypeConstants.BOOL),
    NE("!=", 5, BINARY, StdTypeConstants.BOOL),
    STARTS_WITH("STARTS WITH", 5, BINARY, StdTypeConstants.BOOL),
    LIKE("LIKE", 5, BINARY, StdTypeConstants.BOOL),
    IN("IN", 5, BINARY, StdTypeConstants.BOOL),
    IS_NULL("IS NULL",5, POSTFIX, StdTypeConstants.BOOL),
    IS_NOT_NULL("IS NOT NULL", 5, POSTFIX, StdTypeConstants.BOOL),
    EXISTS("EXISTS", 5, PREFIX, StdTypeConstants.BOOL),
    NOT_EXISTS("NOT EXISTS", 5, PREFIX, StdTypeConstants.BOOL),

    AND("AND", 6, BINARY, StdTypeConstants.BOOL),
    OR("OR", 6, BINARY, StdTypeConstants.BOOL),

    COMMA(",", 7, BINARY, null)


    ;


    private final String op;
    private final int precedence;
    private final int type;
    private final Long resultTypeId;

    Operator(String op, int precedence, int type, Long resultTypeId) {
        this.op = op;
        this.precedence = precedence;
        this.type = type;
        this.resultTypeId = resultTypeId;
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

    public Type resultType(EntityContext context) {
        return NncUtils.get(resultTypeId, context::getType);
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
