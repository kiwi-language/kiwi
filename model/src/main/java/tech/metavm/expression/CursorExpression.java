package tech.metavm.expression;

import tech.metavm.entity.ElementVisitor;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Types;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

@EntityType("游标表达式")
public class CursorExpression extends Expression{

    @EntityField("数组")
    private final Expression array;
    @Nullable
    private final String alias;

    public CursorExpression(Expression array, @Nullable String alias) {
        this.array = NncUtils.requireNonNull(array);
        this.alias = alias;
    }

    @Override
    public String buildSelf(VarType symbolType) {
        return alias;
    }

    public @Nullable String getAlias() {
        return alias;
    }

    @Override
    public int precedence() {
        return 0;
    }

    public Expression getArray() {
        return array;
    }

    @Override
    public ClassType getType() {
        return Types.ensureClassArray(array.getType());
    }

    @Override
    public List<Expression> getChildren() {
        return List.of();
    }

    @Override
    public Expression substituteChildren(List<Expression> children) {
        NncUtils.requireLength(children, 0);
        return new CursorExpression(array, alias);
    }

    @Override
    public Instance evaluate(EvaluationContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CursorExpression that)) return false;
        return Objects.equals(array, that.array) && Objects.equals(alias, that.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(array, alias);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitCursorExpression(this);
    }
}
