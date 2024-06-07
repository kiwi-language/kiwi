package tech.metavm.expression;

import tech.metavm.entity.ElementVisitor;
import tech.metavm.entity.EntityType;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.NeverType;
import tech.metavm.object.type.Type;

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
        return new NeverType();
    }

    @Override
    public List<Expression> getChildren() {
        return List.of();
    }

    @Override
    protected Instance evaluateSelf(EvaluationContext context) {
        throw new UnsupportedOperationException();
    }
}
