package tech.metavm.object.instance.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExpressionFieldValueDTO extends FieldValue {

    private final String expression;

    public ExpressionFieldValueDTO(@JsonProperty("expression") String expression) {
        super(FieldValueKind.EXPRESSION.code(),"");
        this.expression = expression;
    }

    public String getExpression() {
        return expression;
    }

}
