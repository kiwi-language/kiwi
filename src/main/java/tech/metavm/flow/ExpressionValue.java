package tech.metavm.flow;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityType;
import tech.metavm.expression.*;
import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.rest.ExpressionFieldValueDTO;
import tech.metavm.object.instance.rest.FieldValueDTO;
import tech.metavm.object.meta.Type;

@EntityType("表达式值")
public class ExpressionValue extends Value {

    @ChildEntity("表达式")
    private final Expression expression;

    public ExpressionValue(ValueDTO valueDTO, ParsingContext parsingContext) {
        this(ExpressionParser.parse(((ExpressionFieldValueDTO) valueDTO.value()).getExpression(), parsingContext));
    }

    public ExpressionValue(Expression expression) {
        super(ValueKind.EXPRESSION);
        this.expression = expression;
    }

    @Override
    protected FieldValueDTO getDTOValue(boolean persisting) {
        return new ExpressionFieldValueDTO(expression.buildSelf(persisting ? VarType.ID : VarType.NAME));
    }

    @Override
    public Type getType() {
        return expression.getType();
    }

    @Override
    public Instance evaluate(EvaluationContext evaluationContext) {
        return ExpressionEvaluator.evaluate(expression, evaluationContext);
    }

    public Expression getExpression() {
        return expression;
    }
}
