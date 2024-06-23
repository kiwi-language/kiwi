package org.metavm.object.instance.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.Set;

public class ExpressionFieldValue extends FieldValue {

    private final String expression;

    public ExpressionFieldValue(@JsonProperty("expression") String expression) {
        super("");
        this.expression = expression;
    }

    @Override
    public int getKind() {
        return FieldValueKind.EXPRESSION.code();
    }

    public String getExpression() {
        return expression;
    }

    @Override
    public boolean valueEquals(FieldValue that, Set<String> newIds) {
        if(that instanceof ExpressionFieldValue thatExprFieldValue)
            return Objects.equals(expression, thatExprFieldValue.expression);
        else
            return false;
    }

    @Override
    public Object toJson() {
        throw new UnsupportedOperationException();
    }
}
