package org.metavm.expression;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.type.AnyType;
import org.metavm.object.type.Type;

import java.util.List;
import java.util.Objects;

@EntityType
public class VariableExpression extends Expression {

    private final String variable;

    public VariableExpression(@NotNull String variable) {
        this.variable = variable;
    }

    @Override
    public String buildSelf(VarType symbolType, boolean relaxedCheck) {
        return variable;
    }

    @Override
    public int precedence() {
        return 0;
    }

    @Override
    public Type getType() {
        return AnyType.instance;
    }

    @Override
    public List<Expression> getChildren() {
        return List.of();
    }

    @Override
    protected Instance evaluateSelf(EvaluationContext context) {
        throw new UnsupportedOperationException();
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

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitVariableExpression(this);
    }
}
