package tech.metavm.expression;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.object.instance.core.BooleanInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.Type;
import tech.metavm.util.InstanceUtils;

import java.util.List;
import java.util.Objects;

@EntityType("类型检查表达式")
public class InstanceOfExpression extends Expression {

    @ChildEntity("值")
    private final Expression operand;
    @EntityField("目标类型")
    private final Type targetType;

    public InstanceOfExpression(@NotNull Expression operand, @NotNull Type targetType) {
        this.operand = addChild(operand.copy(), "operand");
        this.targetType = targetType;
    }

    @Override
    public String buildSelf(VarType symbolType) {
        try(var context = SerializeContext.enter()) {
            return operand.build(symbolType, false) + " instanceof "
                    + context.getRef(targetType).getIdString();
        }
    }

    @Override
    public int precedence() {
        return 5;
    }

    @Override
    public Type getType() {
        return ModelDefRegistry.getType(Boolean.class);
    }

    @Override
    public List<Expression> getChildren() {
        return List.of(operand);
    }

    @Override
    public Expression substituteChildren(List<Expression> children) {
        return new InstanceOfExpression(children.get(0), targetType);
    }

    @Override
    public BooleanInstance evaluate(EvaluationContext context) {
        return InstanceUtils.booleanInstance(targetType.isInstance(operand.evaluate(context)));
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

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitInstanceOfExpression(this);
    }
}
