package tech.metavm.expression;

import tech.metavm.entity.ElementVisitor;
import tech.metavm.object.type.Type;
import tech.metavm.util.NncUtils;

import java.util.List;
import java.util.Objects;

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
    public List<Expression> getChildren() {
        return List.of(expression);
    }

    @Override
    public Expression substituteChildren(List<Expression> children) {
        NncUtils.requireLength(children, 1);
        return new AsExpression(children.get(0), alias);
    }

    public Expression getExpression() {
        return expression;
    }

    public String getAlias() {
        return alias;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AsExpression that)) return false;
        return Objects.equals(expression, that.expression) && Objects.equals(alias, that.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expression, alias);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitAsExpression(this);
    }
}
