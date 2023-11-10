package tech.metavm.expression;

import tech.metavm.entity.ElementVisitor;
import tech.metavm.object.meta.Type;

import java.util.List;
import java.util.Objects;

public class ConditionalExpression extends Expression {

    private final Expression condition;
    private final Expression trueValue;
    private final Expression falseValue;

    public ConditionalExpression(Expression condition, Expression trueValue, Expression falseValue) {
        this.condition = condition;
        this.trueValue = trueValue;
        this.falseValue = falseValue;
    }

    @Override
    public String buildSelf(VarType symbolType) {
        return condition.build(symbolType, condition.precedence() > precedence())
                + " ? " + trueValue.build(symbolType, trueValue.precedence() >= precedence())
                + " : " + falseValue.build(symbolType, falseValue.precedence() >= precedence());
    }

    @Override
    public int precedence() {
        return Operator.CONDITIONAL.precedence();
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
    public Expression substituteChildren(List<Expression> children) {
        return new ConditionalExpression(children.get(0), children.get(1), children.get(2));
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
