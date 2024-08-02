package org.metavm.expression;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.NeverType;
import org.metavm.object.type.Type;

import java.util.List;

@EntityType
public class ExpressionPlaceholder extends Expression {
    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitExpressionPlaceholder(this);
    }

    @Override
    protected String buildSelf(VarType symbolType, boolean relaxedCheck) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int precedence() {
        return 0;
    }

    @Override
    public Type getType() {
        return NeverType.instance;
    }

    @Override
    public List<Expression> getChildren() {
        return List.of();
    }

    @Override
    protected Value evaluateSelf(EvaluationContext context) {
        throw new UnsupportedOperationException();
    }
}
