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
    protected FieldValue toFieldValue(boolean persisting) {
        return new ExpressionFieldValue(expression.build(persisting ? VarType.ID : VarType.NAME));
    }

    @Override
    public Value copy() {
        return new DynamicValue(getKind(), getExpression().copy());
    }

    @Override
    public Value substituteExpression(Expression expression) {
        return new DynamicValue(getKind(), expression.copy());
    }
}
