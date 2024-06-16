package org.metavm.expression;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.api.EntityType;
import org.metavm.entity.SerializeContext;
import org.metavm.object.instance.core.BooleanInstance;
import org.metavm.object.type.PrimitiveKind;
import org.metavm.object.type.PrimitiveType;
import org.metavm.object.type.Type;
import org.metavm.util.Instances;

import java.util.List;
import java.util.Objects;

@EntityType
public class InstanceOfExpression extends Expression {

    private final Expression operand;
    private final Type targetType;

    public InstanceOfExpression(@NotNull Expression operand, @NotNull Type targetType) {
        this.operand = operand;
        this.targetType = targetType;
    }

    @Override
    public String buildSelf(VarType symbolType, boolean relaxedCheck) {
        try(var serContext = SerializeContext.enter()) {
            return operand.build(symbolType, false, relaxedCheck) + " instanceof "
                    + targetType.toExpression(serContext);
        }
    }

    @Override
    public int precedence() {
        return 5;
    }

    @Override
    public Type getType() {
        return new PrimitiveType(PrimitiveKind.BOOLEAN);
    }

    @Override
    public List<Expression> getChildren() {
        return List.of(operand);
    }

    @Override
    protected BooleanInstance evaluateSelf(EvaluationContext context) {
        return Instances.booleanInstance(targetType.isInstance(operand.evaluate(context)));
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
