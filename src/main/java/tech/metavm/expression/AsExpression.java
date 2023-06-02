package tech.metavm.expression;

import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;

import java.util.List;

public class AsExpression extends Expression {

    private final Expression expression;
    private final String alias;

    public AsExpression(Expression expression, String alias) {
        this.expression = expression;
        this.alias = alias;
    }

    @Override
    public String buildSelf(VarType symbolType) {
        return expression.buildSelf(symbolType) + " AS " + alias;
    }

    @Override
    public int precedence() {
        return 100;
    }

    @Override
    public Type getType() {
        return expression.getType();
    }

    @Override
    protected List<Expression> getChildren() {
        return List.of(expression);
    }

    @Override
    public Expression cloneWithNewChildren(List<Expression> children) {
        NncUtils.requireLength(children, 1);
        return new AsExpression(children.get(0), alias);
    }

    public Expression getExpression() {
        return expression;
    }

    public String getAlias() {
        return alias;
    }
}
