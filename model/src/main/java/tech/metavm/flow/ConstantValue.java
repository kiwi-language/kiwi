package tech.metavm.flow;

import tech.metavm.expression.*;
import tech.metavm.expression.Func;
import tech.metavm.object.instance.core.LongInstance;
import tech.metavm.object.instance.core.TimeInstance;
import tech.metavm.object.instance.rest.ArrayFieldValue;
import tech.metavm.object.instance.rest.FieldValue;
import tech.metavm.object.instance.rest.PrimitiveFieldValue;
import tech.metavm.object.type.PrimitiveKind;
import tech.metavm.util.NncUtils;

import java.util.Date;

public class ConstantValue extends Value {
    public ConstantValue(ValueKind kind, Expression expression) {
        super(kind, expression);
    }

    @Override
    public Value copy() {
        return new ConstantValue(getKind(), getExpression().copy());
    }

    @Override
    protected FieldValue toFieldValue() {
        return switch (expression) {
            case ConstantExpression constantExpression -> constantExpression.getValue().toFieldValueDTO();
            case ArrayExpression arrayExpression -> toArrayFieldValue(arrayExpression);
            case FunctionExpression funcExpr
                    when funcExpr.getFunction() == Func.TIME
                    && funcExpr.getArguments().get(0) instanceof ConstantExpression constExpr ->
                    toTimeFieldValue((LongInstance) constExpr.getValue());
            default -> throw new IllegalStateException("Unexpected value: " + expression);
        };
    }

    private PrimitiveFieldValue toTimeFieldValue(LongInstance timeMillis) {
        var value = timeMillis.getValue();
        return new PrimitiveFieldValue(
                TimeInstance.DF.format(new Date(value)),
                PrimitiveKind.TIME.code(),
                value
        );
    }

    private ArrayFieldValue toArrayFieldValue(ArrayExpression arrayExpression) {
        return new ArrayFieldValue(
                null,
                false,
                NncUtils.map(arrayExpression.getExpressions(), e -> toFieldValue())
        );
    }

}
