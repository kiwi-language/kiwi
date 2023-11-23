package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.entity.EntityType;
import tech.metavm.expression.*;
import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.core.LongInstance;
import tech.metavm.object.instance.rest.ArrayFieldValue;
import tech.metavm.object.instance.rest.FieldValue;
import tech.metavm.object.instance.rest.PrimitiveFieldValue;
import tech.metavm.object.type.PrimitiveKind;
import tech.metavm.object.type.Type;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.Date;

import static tech.metavm.object.instance.core.TimeInstance.DF;

@EntityType("常量值")
public class ConstantValue extends Value {

    @ChildEntity("值")
    private final Expression expression;

    public ConstantValue(ValueDTO valueDTO, @Nullable Type assignedType, ParsingContext parsingContext) {
        super(ValueKind.CONSTANT);
        if (valueDTO.isNull()) {
            expression = addChild(ExpressionUtil.constant(InstanceUtils.nullInstance()), "expression");
        } else {
            expression = addChild(ExpressionParser.parse(
                    ExpressionUtil.constantToExpression(valueDTO.value()),
                    assignedType,
                    parsingContext
            ), "expression");
        }
    }

    public ConstantValue(@NotNull Expression expression) {
        super(ValueKind.CONSTANT);
        this.expression = addChild(expression.copy(), "expression");
    }

    @Override
    protected FieldValue getDTOValue(boolean persisting) {
        return toFieldValue(expression);
    }

    private FieldValue toFieldValue(Expression expression) {
        return switch (expression) {
            case ConstantExpression constantExpression -> ExpressionUtil.expressionToConstant(constantExpression);
            case ArrayExpression arrayExpression -> toArrayFieldValue(arrayExpression);
            case FunctionExpression funcExpr
                    when funcExpr.getFunction() == Function.TIME
                    && funcExpr.getArguments().get(0) instanceof ConstantExpression constExpr ->
                    toTimeFieldValue((LongInstance) constExpr.getValue());
            default -> throw new IllegalStateException("Unexpected value: " + expression);
        };
    }

    private PrimitiveFieldValue toTimeFieldValue(LongInstance timeMillis) {
        var value = timeMillis.getValue();
        return new PrimitiveFieldValue(
                DF.format(new Date(value)),
                PrimitiveKind.TIME.code(),
                value
        );
    }

    private ArrayFieldValue toArrayFieldValue(ArrayExpression arrayExpression) {
        return new ArrayFieldValue(
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

    @Override
    public ConstantValue substituteExpression(Expression expression) {
        return new ConstantValue(expression);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitConstantValue(this);
    }
}
