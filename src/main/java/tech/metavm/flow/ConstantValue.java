package tech.metavm.flow;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityType;
import tech.metavm.expression.*;
import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.rest.ArrayFieldValueDTO;
import tech.metavm.object.instance.rest.FieldValue;
import tech.metavm.object.meta.Type;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;

@EntityType("常量值")
public class ConstantValue extends Value {

    @ChildEntity("值")
    private final Expression expression;

    public ConstantValue(ValueDTO valueDTO, @Nullable Type assignedType, ParsingContext parsingContext) {
        super(ValueKind.CONSTANT);
        if (valueDTO.isNull()) {
            expression = ExpressionUtil.constant(InstanceUtils.nullInstance());
        } else {
            expression = ExpressionParser.parse(
                    ExpressionUtil.constantToExpression(valueDTO.value()),
                    assignedType,
                    parsingContext
            );
        }
    }

    public ConstantValue(Expression expression) {
        super(ValueKind.CONSTANT);
        this.expression = expression;
    }

    @Override
    protected FieldValue getDTOValue(boolean persisting) {
        return toFieldValue(expression);
    }

    private FieldValue toFieldValue(Expression expression) {
        return switch (expression) {
            case ConstantExpression constantExpression -> ExpressionUtil.expressionToConstant(constantExpression);
            case ArrayExpression arrayExpression -> toArrayFieldValue(arrayExpression);
            default -> throw new IllegalStateException("Unexpected value: " + expression);
        };
    }

    private ArrayFieldValueDTO toArrayFieldValue(ArrayExpression arrayExpression) {
        return new ArrayFieldValueDTO(
                null,
                false,
                NncUtils.map(arrayExpression.getExpressions(), this::toFieldValue)
        );
    }

    @Override
    public Type getType() {
        return expression.getType();
    }

    @Override
    public Instance evaluate(EvaluationContext context) {
        return ExpressionEvaluator.evaluate(expression, context);
    }

    @Override
    public Expression getExpression() {
        return expression;
    }

    @Override
    public ConstantValue copy() {
        return new ConstantValue(expression.copy());
    }

}
