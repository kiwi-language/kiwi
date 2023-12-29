package tech.metavm.flow;

import tech.metavm.expression.Expression;
import tech.metavm.expression.VarType;
import tech.metavm.object.instance.rest.ExpressionFieldValue;
import tech.metavm.object.instance.rest.FieldValue;

public class DynamicValue extends Value {

    public DynamicValue(ValueKind kind, Expression expression) {
        super(kind, expression);
    }

    @Override
    protected FieldValue toFieldValue() {
        return new ExpressionFieldValue(expression.build(VarType.NAME));
    }

    @Override
    public Value copy() {
        return new DynamicValue(getKind(), getExpression().copy());
    }

}
