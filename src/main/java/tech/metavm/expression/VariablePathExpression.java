package tech.metavm.expression;

import tech.metavm.entity.ElementVisitor;
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
        return qualifier.build(symbolType, false) + "." + field.buildSelf(symbolType);
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
    public List<Expression> getChildren() {
        return List.of(qualifier, field);
    }

    @Override
    public Expression substituteChildren(List<Expression> children) {
        return new VariablePathExpression(children.get(0), (VariableExpression) children.get(1));
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
