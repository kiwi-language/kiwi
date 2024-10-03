package org.metavm.expression;

import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;

import java.util.List;

public class NeverExpression extends Expression {
    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitNeverExpression(this);
    }

    @Override
    protected String buildSelf(VarType symbolType, boolean relaxedCheck) {
        return "never";
    }

    @Override
    public int precedence() {
        return 0;
    }

    @Override
    public Type getType() {
        return Types.getNeverType();
    }

    @Override
    public List<Expression> getChildren() {
        return List.of();
    }

    @Override
    protected Value evaluateSelf(EvaluationContext context) {
        throw new IllegalStateException("NeverExpression should not be evaluated");
    }
}
