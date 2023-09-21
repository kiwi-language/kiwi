package tech.metavm.expression;

import tech.metavm.object.meta.Type;

import java.util.List;
import java.util.Objects;

public class VariablePathExpression extends Expression {

    private final Expression qualifier;
    private final VariableExpression field;

    public VariablePathExpression(Expression qualifier, VariableExpression field) {
        this.qualifier = qualifier;
        this.field = field;
    }

    public Expression getQualifier() {
        return qualifier;
    }

    public VariableExpression getField() {
        return field;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VariablePathExpression that)) return false;
        return Objects.equals(qualifier, that.qualifier) && Objects.equals(field, that.field);
    }

    @Override
    public int hashCode() {
        return Objects.hash(qualifier, field);
    }
}
