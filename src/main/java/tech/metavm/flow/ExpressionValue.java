package tech.metavm.flow;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.entity.EntityParentRef;
import tech.metavm.entity.EntityType;
import tech.metavm.expression.*;
import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.rest.ExpressionFieldValue;
import tech.metavm.object.instance.rest.FieldValue;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;

@EntityType("表达式值")
public class ExpressionValue extends Value {

    @ChildEntity("表达式")
    private final Expression expression;

    public ExpressionValue(ValueDTO valueDTO, ParsingContext parsingContext) {
        this(ExpressionParser.parse(((ExpressionFieldValue) valueDTO.value()).getExpression(), parsingContext));
    }

    public ExpressionValue(Expression expression) {
        super(ValueKind.EXPRESSION);
        this.expression = NncUtils.requireNonNull(expression);
    }

    @Override
    protected FieldValue getDTOValue(boolean persisting) {
        return new ExpressionFieldValue(expression.build(persisting ? VarType.ID : VarType.NAME));
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

    @Override
    public ExpressionValue copy() {
        return new ExpressionValue(expression.copy());
    }

    @Override
    public ExpressionValue substituteExpression(Expression expression) {
        return new ExpressionValue(expression);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitExpressionValue(this);
    }
}
