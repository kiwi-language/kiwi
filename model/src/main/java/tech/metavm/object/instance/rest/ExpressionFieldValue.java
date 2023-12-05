package tech.metavm.object.instance.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExpressionFieldValue extends FieldValue {

    private final String expression;

    public ExpressionFieldValue(@JsonProperty("expression") String expression) {
        super(FieldValueKind.EXPRESSION.code(),"");
        this.expression = expression;
    }

    public String getExpression() {
        return expression;
    }

}
