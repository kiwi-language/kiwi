package tech.metavm.flow;

import tech.metavm.flow.rest.ValueDTO;

public class ExpressionValue extends Value{

    private String expression;

    public ExpressionValue(ValueDTO valueDTO) {
        super(valueDTO);
        expression = (String) valueDTO.value();
    }

    @Override
    protected Object getDTOValue() {
        return expression;
    }

    @Override
    public Object evaluate(FlowFrame frame) {
        return null;
    }
}
