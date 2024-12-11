package org.metavm.expression;

import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.ClassType;

import java.util.List;
import java.util.Objects;

@Entity
public class ThisExpression extends Expression {

    private final ClassType type;

    public ThisExpression(ClassType type) {
        this.type = type;
    }

    @Override
    public String buildSelf(VarType symbolType, boolean relaxedCheck) {
        return "this";
    }

    @Override
    public int precedence() {
        return 0;
    }

    @Override
    public ClassType getType() {
        return type;
    }

    @Override
    public List<Expression> getChildren() {
        return List.of();
    }

    @Override
    protected Value evaluateSelf(EvaluationContext context) {
        return context.evaluate(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ThisExpression that)) return false;
        return Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitThisExpression(this);
    }
}
