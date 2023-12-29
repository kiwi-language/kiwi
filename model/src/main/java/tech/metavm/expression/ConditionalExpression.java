package tech.metavm.expression;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.object.instance.core.BooleanInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.Type;

import java.util.List;
import java.util.Objects;

@EntityType("条件表达式")
public class ConditionalExpression extends Expression {

    @EntityField("条件")
    private final Expression condition;
    @EntityField("true表达式")
    private final Expression trueValue;
    @EntityField("false表达式")
    private final Expression falseValue;

    public ConditionalExpression(@NotNull Expression condition,
                                 @NotNull Expression trueValue,
                                 @NotNull Expression falseValue) {
        this.condition = addChild(condition.copy(), "condition");
        this.trueValue = addChild(trueValue.copy(), "trueValue");
        this.falseValue = addChild(falseValue.copy(), "falseValue");
    }

    @Override
    public String buildSelf(VarType symbolType) {
        return condition.build(symbolType, condition.precedence() > precedence())
                + " ? " + trueValue.build(symbolType, trueValue.precedence() >= precedence())
                + " : " + falseValue.build(symbolType, falseValue.precedence() >= precedence());
    }

    @Override
    public int precedence() {
        return 10;
    }

    @Override
    public Type getType() {
        return trueValue.getType();
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
    public Instance evaluate(EvaluationContext context) {
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
