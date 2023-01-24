package tech.metavm.object.instance.query;

public record Token (
        TokenType type,
        String rawValue,
        Object value
) {

    public String getName() {
        return (String) value;
    }

    public Operator getOperator() {
        return (Operator) value;
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

    public boolean isFunction() {
        return type == TokenType.FUNCTION;
    }

    public boolean isLeftParenthesis() {
        return type == TokenType.LEFT_PARENTHESIS;
    }

    public boolean isRightParenthesis() {
        return type == TokenType.RIGHT_PARENTHESIS;
    }

    public boolean isLeftBracket() {
        return type == TokenType.LEFT_BRACKET;
    }

    public boolean isRightBracket() {
        return type == TokenType.RIGHT_BRACKET;
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
