package tech.metavm.expression;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.Type;

import java.util.List;
import java.util.Objects;

@EntityType("别名表达式")
public class AsExpression extends Expression {

    @ChildEntity("表达式")
    private final Expression expression;
    @EntityField("别名")
    private final String alias;

    public AsExpression(@NotNull Expression expression, @NotNull String alias) {
        this.expression = addChild(expression.copy(), "expression");
        this.alias = alias;
    }

    @Override
    public String buildSelf(VarType symbolType) {
        return expression.buildSelf(symbolType) + " as " + alias;
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
