package tech.metavm.flow;

import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.object.instance.query.*;

public class ExpressionValue extends Value {

    private final Expression expression;

    public ExpressionValue(ValueDTO valueDTO, ParsingContext parsingContext) {
        super(valueDTO);
        String str = (String) valueDTO.value();
        expression = ExpressionParser.parse(str, parsingContext);
    }

    @Override
    protected Object getDTOValue(boolean persisting) {
        return expression.buildSelf(persisting ? VarType.ID : VarType.NAME);
    }

    @Override
    public Object evaluate(EvaluationContext evaluationContext) {
        return ExpressionEvaluator.evaluate(expression, evaluationContext);
    }
}
