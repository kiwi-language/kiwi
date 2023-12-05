package tech.metavm.flow;

import tech.metavm.expression.*;
import tech.metavm.object.instance.core.LongInstance;
import tech.metavm.object.instance.core.TimeInstance;
import tech.metavm.object.instance.rest.ArrayFieldValue;
import tech.metavm.object.instance.rest.FieldValue;
import tech.metavm.object.instance.rest.PrimitiveFieldValue;
import tech.metavm.object.type.PrimitiveKind;
import tech.metavm.util.NncUtils;

import java.util.Date;

import static tech.metavm.object.instance.core.TimeInstance.DF;

public class ConstantValue extends Value {
    public ConstantValue(ValueKind kind, Expression expression) {
        super(kind, expression);
    }

    @Override
    public Value copy() {
        return new ConstantValue(getKind(), getExpression().copy());
    }

    @Override
    public Value substituteExpression(Expression expression) {
        return new ConstantValue(getKind(), expression.copy());
    }

    @Override
    protected FieldValue toFieldValue(boolean persisting) {
        return switch (expression) {
            case ConstantExpression constantExpression -> constantExpression.getValue().toFieldValueDTO();
            case ArrayExpression arrayExpression -> toArrayFieldValue(arrayExpression, persisting);
            case FunctionExpression funcExpr
                    when funcExpr.getFunction() == Function.TIME
                    && funcExpr.getArguments().get(0) instanceof ConstantExpression constExpr ->
                    toTimeFieldValue((LongInstance) constExpr.getValue(), persisting);
            default -> throw new IllegalStateException("Unexpected value: " + expression);
        };
    }

    private PrimitiveFieldValue toTimeFieldValue(LongInstance timeMillis, boolean persisting) {
        var value = timeMillis.getValue();
        return new PrimitiveFieldValue(
                TimeInstance.DF.format(new Date(value)),
                PrimitiveKind.TIME.code(),
                value
        );
    }

    private ArrayFieldValue toArrayFieldValue(ArrayExpression arrayExpression, boolean persisting) {
        return new ArrayFieldValue(
                null,
                false,
                NncUtils.map(arrayExpression.getExpressions(), e -> toFieldValue(persisting))
        );
    }

}
