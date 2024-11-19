package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.expression.EvaluationContext;
import org.metavm.expression.Expression;
import org.metavm.object.type.Type;

public class NeverValue extends Value {

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public org.metavm.object.instance.core.@NotNull Value evaluate(EvaluationContext context) {
        throw new IllegalStateException("NeverValue should not be evaluated");
    }

    @Override
    public String getText() {
        return null;
    }

    @Override
    public Expression getExpression() {
        return null;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitNeverValue(this);
    }

}
