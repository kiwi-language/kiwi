package tech.metavm.expression;

import tech.metavm.entity.ElementVisitor;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.Type;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

@EntityType("游标表达式")
public class CursorExpression extends Expression{

    @EntityField("别名")
    private final @Nullable String alias;
    @EntityField("类型")
    private final Type type;

    public CursorExpression(Type type, @Nullable String alias) {
        this.type = type;
        this.alias = alias;
    }

    @Override
    public String buildSelf(VarType symbolType, boolean relaxedCheck) {
        return alias;
    }

    public @Nullable String getAlias() {
        return alias;
    }

    @Override
    public int precedence() {
        return 0;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public List<Expression> getChildren() {
        return List.of();
    }

    @Override
    protected Instance evaluateSelf(EvaluationContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CursorExpression that)) return false;
        return Objects.equals(type, that.type) && Objects.equals(alias, that.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, alias);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitCursorExpression(this);
    }
}
