package tech.metavm.expression;

import tech.metavm.object.meta.Type;

import java.util.List;
import java.util.Objects;

public class VariableExpression extends Expression {

    private final String variable;

    public VariableExpression(String variable) {
        this.variable = variable;
    }

    @Override
    public String buildSelf(VarType symbolType) {
        return variable;
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
        return new VariableExpression(variable);
    }

    public String getVariable() {
        return variable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VariableExpression that)) return false;
        return Objects.equals(variable, that.variable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variable);
    }
}
