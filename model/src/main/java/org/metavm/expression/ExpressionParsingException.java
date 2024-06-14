package org.metavm.expression;

public class ExpressionParsingException extends RuntimeException  {

    public ExpressionParsingException() {
    }

    public ExpressionParsingException(String message) {
        super(message);
    }
}
