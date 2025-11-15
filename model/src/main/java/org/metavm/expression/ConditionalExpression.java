package org.metavm.expression;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.wire.Wire;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.BooleanValue;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

@Wire
@Entity
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

    @Generated
    public static ConditionalExpression read(MvInput input) {
        return new ConditionalExpression(Expression.read(input), Expression.read(input), Expression.read(input), input.readType());
    }

    @Generated
    public static void visit(StreamVisitor visitor) {
        Expression.visit(visitor);
        Expression.visit(visitor);
        Expression.visit(visitor);
        visitor.visitValue();
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
    public List<Expression> getComponents() {
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
    protected Value evaluateSelf(EvaluationContext context) {
        return ((BooleanValue) condition.evaluate(context)).getValue() ?
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

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
        condition.accept(visitor);
        trueValue.accept(visitor);
        falseValue.accept(visitor);
        type.accept(visitor);
    }

    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        condition.forEachReference(action);
        trueValue.forEachReference(action);
        falseValue.forEachReference(action);
        type.forEachReference(action);
    }

    @Generated
    public void write(MvOutput output) {
        output.write(TYPE_ConditionalExpression);
        super.write(output);
        condition.write(output);
        trueValue.write(output);
        falseValue.write(output);
        output.writeValue(type);
    }

    @Override
    public Expression transform(ExpressionTransformer transformer) {
        return new ConditionalExpression(
                condition.accept(transformer),
                trueValue.accept(transformer),
                falseValue.accept(transformer),
                type
        );
    }
}
