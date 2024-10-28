package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.expression.*;
import org.metavm.object.instance.core.LongValue;
import org.metavm.object.instance.core.TimeValue;
import org.metavm.object.instance.rest.ArrayFieldValue;
import org.metavm.object.instance.rest.FieldValue;
import org.metavm.object.instance.rest.NeverFieldValue;
import org.metavm.object.instance.rest.PrimitiveFieldValue;
import org.metavm.object.type.PrimitiveKind;
import org.metavm.util.NncUtils;

import java.util.Date;

@EntityType
public class ConstantValue extends Value {
    public ConstantValue(ValueKind kind, Expression expression) {
        super(kind, expression);
    }

    @Override
    protected FieldValue toFieldValue() {
        return switch (expression) {
            case ConstantExpression constantExpression -> constantExpression.getValue().toFieldValueDTO();
            case ArrayExpression arrayExpression -> toArrayFieldValue(arrayExpression);
            case NeverExpression neverExpr -> new NeverFieldValue();
            default -> throw new IllegalStateException("Unexpected value: " + expression);
        };
    }

    private PrimitiveFieldValue toTimeFieldValue(LongValue timeMillis) {
        var value = timeMillis.getValue();
        return new PrimitiveFieldValue(
                TimeValue.DF.format(new Date(value)),
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
