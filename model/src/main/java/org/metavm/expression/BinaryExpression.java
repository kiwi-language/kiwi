package org.metavm.expression;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.Utils;
import org.metavm.util.StreamVisitor;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Entity
public class BinaryExpression extends Expression {
    @SuppressWarnings("unused")
    private static org.metavm.object.type.Klass __klass__;
    private final BinaryOperator operator;
    private final Expression left;
    private final Expression right;

    public BinaryExpression(@NotNull BinaryOperator operator, Expression left, Expression right) {
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    @Generated
    public static BinaryExpression read(MvInput input) {
        return new BinaryExpression(BinaryOperator.fromCode(input.read()), Expression.read(input), Expression.read(input));
    }

    @Generated
    public static void visit(StreamVisitor visitor) {
        visitor.visitByte();
        Expression.visit(visitor);
        Expression.visit(visitor);
    }

    public BinaryOperator getOperator() {
        return operator;
    }

    public Expression getLeft() {
        return left;
    }

    public Expression getRight() {
        return right;
    }

    @Override
    public String buildSelf(VarType symbolType, boolean relaxedCheck) {
        String firstExpr = left.build(symbolType, left.precedence() > precedence(), relaxedCheck);
        String secondExpr = right.build(symbolType, right.precedence() >= precedence(), relaxedCheck);
        return firstExpr + " " + operator + " " + secondExpr;
    }

    @Override
    public int precedence() {
        return operator.precedence();
    }

    @Override
    public Type getType() {
        if(operator.resultType() != null) {
            return operator.resultType();
        }
        return Types.getCompatibleType(left.getType(), right.getType());
    }

    @Override
    public List<Expression> getComponents() {
        return List.of(left, right);
    }

    @Override
    protected Value evaluateSelf(EvaluationContext context) {
        return operator.evaluate(left.evaluate(context), right.evaluate(context));
    }

    @Override
    public <T extends Expression> List<T> extractExpressionsRecursively(Class<T> klass) {
        return Utils.union(left.extractExpressions(klass), right.extractExpressions(klass));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BinaryExpression that)) return false;
        return operator == that.operator && Objects.equals(left, that.left) && Objects.equals(right, that.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operator, left, right);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitBinaryExpression(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
        left.accept(visitor);
        right.accept(visitor);
    }

    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        left.forEachReference(action);
        right.forEachReference(action);
    }

    public void buildJson(java.util.Map<String, Object> map) {
        map.put("operator", this.getOperator().name());
        map.put("left", this.getLeft().toJson());
        map.put("right", this.getRight().toJson());
        map.put("type", this.getType().toJson());
        map.put("components", this.getComponents().stream().map(Expression::toJson).toList());
        map.put("variableComponent", this.getVariableComponent().toJson());
        map.put("constantComponent", this.getConstantComponent().toJson());
        map.put("fieldComponent", this.getFieldComponent().toJson());
        map.put("arrayComponent", this.getArrayComponent().toJson());
    }

    @Generated
    public void write(MvOutput output) {
        output.write(TYPE_BinaryExpression);
        super.write(output);
        output.write(operator.code());
        left.write(output);
        right.write(output);
    }
}
