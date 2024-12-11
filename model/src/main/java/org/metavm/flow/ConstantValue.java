package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.expression.ConstantExpression;
import org.metavm.expression.EvaluationContext;
import org.metavm.expression.Expression;
import org.metavm.object.type.Type;

@Entity
public class ConstantValue extends Value {

    private final org.metavm.object.instance.core.Value value;

    public ConstantValue(org.metavm.object.instance.core.Value value) {
        this.value = value;
    }

    @Override
    public Type getType() {
        return value.getType();
    }

    @Override
    public org.metavm.object.instance.core.@NotNull Value evaluate(EvaluationContext context) {
        return value;
    }

    @Override
    public String getText() {
        return value.getText();
    }

    @Override
    public Expression getExpression() {
        return new ConstantExpression(value);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitConstantValue(this);
    }
}
