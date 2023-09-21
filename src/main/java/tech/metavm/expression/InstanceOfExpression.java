package tech.metavm.expression;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.object.meta.Type;

import java.util.List;
import java.util.Objects;

@EntityType("类型检查表达式")
public class InstanceOfExpression extends Expression {

    private final Expression operand;
    private final Type targetType;

    public InstanceOfExpression(Expression operand, Type targetType) {
        this.operand = operand;
        this.targetType = targetType;
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
        return ModelDefRegistry.getType(Boolean.class);
    }

    @Override
    protected List<Expression> getChildren() {
        return null;
    }

    @Override
    public Expression cloneWithNewChildren(List<Expression> children) {
        return null;
    }

    public Expression getOperand() {
        return operand;
    }

    public Type getTargetType() {
        return targetType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InstanceOfExpression that)) return false;
        return Objects.equals(operand, that.operand) && Objects.equals(targetType, that.targetType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operand, targetType);
    }
}
