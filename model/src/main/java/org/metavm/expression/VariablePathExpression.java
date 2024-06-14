package org.metavm.expression;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.EntityType;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.type.AnyType;
import org.metavm.object.type.Type;

import java.util.List;
import java.util.Objects;

@EntityType
public class VariablePathExpression extends Expression {

    private final Expression qualifier;
    private final VariableExpression field;

    public VariablePathExpression(@NotNull Expression qualifier, @NotNull VariableExpression field) {
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
    public String buildSelf(VarType symbolType, boolean relaxedCheck) {
        return qualifier.build(symbolType, false, relaxedCheck) + "." + field.buildSelf(symbolType, relaxedCheck);
    }

    @Override
    public int precedence() {
        return 0;
    }

    @Override
    public Type getType() {
        return new AnyType();
    }

    @Override
    public List<Expression> getChildren() {
        return List.of(qualifier, field);
    }

    @Override
    protected Instance evaluateSelf(EvaluationContext context) {
        throw new UnsupportedOperationException();
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

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitVariablePathExpression(this);
    }
}
