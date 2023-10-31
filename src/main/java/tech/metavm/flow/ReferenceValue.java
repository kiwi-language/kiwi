package tech.metavm.flow;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityType;
import tech.metavm.expression.*;
import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.rest.ExpressionFieldValueDTO;
import tech.metavm.object.instance.rest.FieldValue;
import tech.metavm.object.meta.Type;

@EntityType("引用值")
public class ReferenceValue extends Value {

    @ChildEntity("表达式")
    private final Expression expression;

    public ReferenceValue(ValueDTO valueDTO, ParsingContext parsingContext) {
        super(ValueKind.REFERENCE);
        ExpressionFieldValueDTO exprValue = (ExpressionFieldValueDTO) valueDTO.value();
        expression = ExpressionParser.parse(exprValue.getExpression(), parsingContext);
    }

    public ReferenceValue(Expression expression) {
        super(ValueKind.REFERENCE);
        this.expression = expression;
    }

    @Override
    protected FieldValue getDTOValue(boolean persisting) {
        return new ExpressionFieldValueDTO(expression.buildSelf(persisting ? VarType.ID : VarType.NAME));
    }

    @Override
    public Type getType() {
        return expression.getType();
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public Instance evaluate(EvaluationContext context) {
        return ExpressionEvaluator.evaluate(expression, context);
    }

}
