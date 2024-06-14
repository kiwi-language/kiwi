package org.metavm.expression;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.EntityType;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.type.Type;

import java.util.List;
import java.util.Objects;

@EntityType
public class AsExpression extends Expression {

    private final Expression expression;
    private final String alias;

    public AsExpression(@NotNull Expression expression, @NotNull String alias) {
        this.expression = expression;
        this.alias = alias;
    }

    @Override
    public String buildSelf(VarType symbolType, boolean relaxedCheck) {
        return expression.buildSelf(symbolType, relaxedCheck) + " as " + alias;
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
    protected Instance evaluateSelf(EvaluationContext context) {
        return expression.evaluate(context);
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
