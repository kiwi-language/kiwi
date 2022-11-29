package tech.metavm.flow;

import tech.metavm.entity.ValueType;
import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.object.instance.query.*;
import tech.metavm.object.meta.Type;

@ValueType("表达式值")
public class ExpressionValue extends Value {

    private final Expression expression;

    public ExpressionValue(ValueDTO valueDTO, ParsingContext parsingContext) {
        super(valueDTO/*, parsingContext.getInstanceContext()*/);
        String str = (String) valueDTO.value();
        expression = ExpressionParser.parse(str, parsingContext);
    }

    @Override
    protected Object getDTOValue(boolean persisting) {
        return expression.buildSelf(persisting ? VarType.ID : VarType.NAME);
    }

    @Override
    public Type getType() {
        return expression.getType();
    }

    @Override
    public Object evaluate(EvaluationContext evaluationContext) {
        return ExpressionEvaluator.evaluate(expression, evaluationContext);
    }

    public Expression getExpression() {
        return expression;
    }
}
