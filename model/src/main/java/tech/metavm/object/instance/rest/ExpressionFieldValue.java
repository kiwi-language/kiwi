package tech.metavm.object.instance.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class ExpressionFieldValue extends FieldValue {

    private final String expression;

    public ExpressionFieldValue(@JsonProperty("expression") String expression) {
        super(FieldValueKind.EXPRESSION.code(),"");
        this.expression = expression;
    }

    public String getExpression() {
        return expression;
    }

    @Override
    public boolean valueEquals(FieldValue that) {
        if(that instanceof ExpressionFieldValue thatExprFieldValue)
            return Objects.equals(expression, thatExprFieldValue.expression);
        else
            return false;
    }
}
