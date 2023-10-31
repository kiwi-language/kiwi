package tech.metavm.expression;

import tech.metavm.flow.Flow;
import tech.metavm.object.meta.Type;

import java.util.List;

public class ConstructorExpression extends Expression {

    private final Flow flow;

    public ConstructorExpression(Flow flow) {
        this.flow = flow;
    }

    @Override
    public String buildSelf(VarType symbolType) {
        return null;
    }

    @Override
    public int precedence() {
        return 0;
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    protected List<Expression> getChildren() {
        return null;
    }

    @Override
    public Expression cloneWithNewChildren(List<Expression> children) {
        return null;
    }
}
