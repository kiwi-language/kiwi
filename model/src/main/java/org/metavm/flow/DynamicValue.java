package org.metavm.flow;

import org.metavm.entity.EntityType;
import org.metavm.expression.Expression;
import org.metavm.expression.VarType;
import org.metavm.object.instance.rest.ExpressionFieldValue;
import org.metavm.object.instance.rest.FieldValue;

@EntityType
public class DynamicValue extends Value {

    public DynamicValue(ValueKind kind, Expression expression) {
        super(kind, expression);
    }

    @Override
    protected FieldValue toFieldValue() {
        return new ExpressionFieldValue(expression.build(VarType.NAME));
    }

}
