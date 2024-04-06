package tech.metavm.expression;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.object.instance.core.BooleanInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.Types;
import tech.metavm.object.type.UnionTypeProvider;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@EntityType("条件表达式")
public class ConditionalExpression extends Expression {

    public static ConditionalExpression create(@NotNull Expression condition,
                                               @NotNull Expression trueValue,
                                               @NotNull Expression falseValue,
                                               UnionTypeProvider unionTypeProvider) {
        return new ConditionalExpression(condition, trueValue, falseValue, Types.getUnionType(
                Set.of(trueValue.getType(), falseValue.getType()), unionTypeProvider));
    }

    @ChildEntity("条件")
    private final Expression condition;
    @ChildEntity("true表达式")
    private final Expression trueValue;
    @ChildEntity("false表达式")
    private final Expression falseValue;
    @EntityField("类型")
    private final Type type;

    public ConditionalExpression(@NotNull Expression condition,
                                 @NotNull Expression trueValue,
                                 @NotNull Expression falseValue,
                                 Type type) {
        this.condition = addChild(condition.copy(), "condition");
        this.trueValue = addChild(trueValue.copy(), "trueValue");
        this.falseValue = addChild(falseValue.copy(), "falseValue");
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
