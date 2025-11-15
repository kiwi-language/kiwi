package org.metavm.expression;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.wire.Wire;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.SerializeContext;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.PrimitiveType;
import org.metavm.object.type.Type;
import org.metavm.util.Instances;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Getter
@Wire
@Entity
public class InstanceOfExpression extends Expression {

    private final Expression operand;
    private final Type targetType;

    public InstanceOfExpression(@NotNull Expression operand, @NotNull Type targetType) {
        this.operand = operand;
        this.targetType = targetType;
    }

    @Generated
    public static InstanceOfExpression read(MvInput input) {
        return new InstanceOfExpression(Expression.read(input), input.readType());
    }

    @Generated
    public static void visit(StreamVisitor visitor) {
        Expression.visit(visitor);
        visitor.visitValue();
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
        return PrimitiveType.booleanType;
    }

    @Override
    public List<Expression> getComponents() {
        return List.of(operand);
    }

    @Override
    protected Value evaluateSelf(EvaluationContext context) {
        return Instances.intInstance(targetType.isInstance(operand.evaluate(context)));
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

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
        operand.accept(visitor);
        targetType.accept(visitor);
    }

    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        operand.forEachReference(action);
        targetType.forEachReference(action);
    }

    @Generated
    public void write(MvOutput output) {
        output.write(TYPE_InstanceOfExpression);
        super.write(output);
        operand.write(output);
        output.writeValue(targetType);
    }

    @Override
    public Expression transform(ExpressionTransformer transformer) {
        return new InstanceOfExpression(operand.accept(transformer), targetType);
    }
}
