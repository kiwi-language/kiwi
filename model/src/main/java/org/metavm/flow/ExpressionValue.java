package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.expression.EvaluationContext;
import org.metavm.expression.Expression;
import org.metavm.expression.VarType;
import org.metavm.object.type.Type;

@EntityType
public class ExpressionValue extends Value {

    private final Expression expression;

    public ExpressionValue(@NotNull Expression expression) {
        this.expression = expression;
    }

    @Override
    public Type getType() {
        return expression.getType();
    }

    @Override
    public org.metavm.object.instance.core.@NotNull Value evaluate(EvaluationContext context) {
        return expression.evaluate(context);
    }

    @Override
    public String getText() {
        return expression.build(VarType.NAME);
    }

    @Override
    public Expression getExpression() {
        return expression;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitExpressionValue(this);
    }
}
