package tech.metavm.expression;

import tech.metavm.util.Constants;

public record Token (
        TokenType type,
        String rawValue,
        Object value
) {

    public static final String ALL_MATCH = "AllMatch";

    public static final String AS = "AS";

    public String getName() {
        return (String) value;
    }

    public BinaryOperator getOperator() {
        return (BinaryOperator) value;
    }

    public Function getFunction() {
        return (Function) value;
    }

    public Long getLong() {
        return (Long) value;
    }

    public Double getDouble() {
        return (Double) value;
    }

    public String getString() {
        return (String) value;
    }

    public boolean isOperator() {
        return type == TokenType.OPERATOR;
    }

    public boolean isLt() {
        return type == TokenType.OPERATOR && getOperator() == BinaryOperator.LT;
    }

    public boolean isGt() {
        return type == TokenType.OPERATOR && getOperator() == BinaryOperator.GT;
    }

    public boolean isFunction() {
        return type == TokenType.FUNCTION;
    }

    public boolean isAllMatch() {
        return type == TokenType.KEYWORD && rawValue.equalsIgnoreCase(ALL_MATCH);
    }

    public boolean isAs() {
        return type == TokenType.KEYWORD && rawValue.equalsIgnoreCase(AS);
    }

    public boolean isOpenParenthesis() {
        return type == TokenType.OPEN_PARENTHESIS;
    }

    public boolean isClosingParenthesis() {
        return type == TokenType.CLOSING_PARENTHESIS;
    }

    public boolean isOpenBracket() {
        return type == TokenType.OPEN_BRACKET;
    }

    public boolean isClosingBracket() {
        return type == TokenType.CLOSING_BRACKET;
    }

    public boolean isConstant() {
        return isInt() || isFloat() || isNull() || isString() || isBoolean();
    }

    public boolean isString() {
        return type == TokenType.SINGLE_QUOTED_STRING || type == TokenType.DOUBLE_QUOTED_STRING;
    }

    public boolean isInt() {
        return type == TokenType.INTEGER;
    }

    public boolean isFloat() {
        return type == TokenType.FLOAT;
    }

    public boolean isNull() {
        return type == TokenType.NULL;
    }

    public boolean isBoolean() {
        return type == TokenType.BOOLEAN;
    }

    public boolean isIdConstant() {
        return type == TokenType.VARIABLE && rawValue.startsWith(Constants.CONSTANT_ID_PREFIX);
    }

    public boolean isTmpIdConstant() {
        return type == TokenType.VARIABLE && rawValue.startsWith(Constants.CONSTANT_TMP_ID_PREFIX);
    }

    public boolean isVariable() {
        return type == TokenType.VARIABLE;
    }

    @Override
    public String toString() {
        return "{" +
                "category: " + type +
                ", value: '" + value +
                "'}";
    }
}
