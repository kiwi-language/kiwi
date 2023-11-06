package tech.metavm.expression;

import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.TypeUtils;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class CursorExpression extends Expression{

    private final Expression array;
    @Nullable
    private final String alias;

    public CursorExpression(Expression array, @Nullable String alias) {
        this.array = array;
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
        return TypeUtils.ensureClassArray(array.getType());
    }

    @Override
    protected List<Expression> getChildren() {
        return List.of();
    }

    @Override
    public Expression cloneWithNewChildren(List<Expression> children) {
        NncUtils.requireLength(children, 0);
        return new CursorExpression(array, alias);
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
}
