package org.metavm.expression;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.EntityType;
import org.metavm.object.instance.core.BooleanInstance;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@EntityType
public class ConditionalExpression extends Expression {

    public static ConditionalExpression create(@NotNull Expression condition,
                                               @NotNull Expression trueValue,
                                               @NotNull Expression falseValue) {
        return new ConditionalExpression(condition, trueValue, falseValue, Types.getUnionType(
                Set.of(trueValue.getType(), falseValue.getType())));
    }

    private final Expression condition;
    private final Expression trueValue;
    private final Expression falseValue;
    private final Type type;

    public ConditionalExpression(@NotNull Expression condition,
                                 @NotNull Expression trueValue,
                                 @NotNull Expression falseValue,
                                 @NotNull Type type) {
        this.condition = condition;
        this.trueValue = trueValue;
        this.falseValue = falseValue;
        this.type = type;
    }

    @Override
    public String buildSelf(VarType symbolType, boolean relaxedCheck) {
        return condition.build(symbolType, condition.precedence() > precedence(), relaxedCheck)
                + " ? " + trueValue.build(symbolType, trueValue.precedence() >= precedence(), relaxedCheck)
                + " : " + falseValue.build(symbolType, falseValue.precedence() >= precedence(), relaxedCheck);
    }

    @Override
    public int precedence() {
        return 10;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public List<Expression> getChildren() {
        return List.of(condition, trueValue, falseValue);
    }

    public Expression getCondition() {
        return condition;
    }

    public Expression getTrueValue() {
        return trueValue;
    }

    public Expression getFalseValue() {
        return falseValue;
    }

    @Override
    protected Instance evaluateSelf(EvaluationContext context) {
        return ((BooleanInstance) condition.evaluate(context)).getValue() ?
                trueValue.evaluate(context) : falseValue.evaluate(context);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConditionalExpression that)) return false;
        return Objects.equals(condition, that.condition) && Objects.equals(trueValue, that.trueValue) && Objects.equals(falseValue, that.falseValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(condition, trueValue, falseValue);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitConditionalExpression(this);
    }
}
