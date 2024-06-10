package tech.metavm.expression;

import tech.metavm.entity.ElementVisitor;
import tech.metavm.entity.EntityType;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.Type;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ValueUtils;

import java.util.List;
import java.util.Objects;

@EntityType
public class BinaryExpression extends Expression {
    private final BinaryOperator operator;
    private final Expression left;
    private final Expression right;

    public BinaryExpression(BinaryOperator operator, Expression left, Expression right) {
        this.operator = operator;
        this.left = left;
        this.right = right;
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
        return ValueUtils.getCompatibleType(left.getType(), right.getType());
    }

    @Override
    public List<Expression> getChildren() {
        return List.of(left, right);
    }

    @Override
    protected Instance evaluateSelf(EvaluationContext context) {
        return operator.evaluate(left.evaluate(context), right.evaluate(context));
    }

    @Override
    public <T extends Expression> List<T> extractExpressionsRecursively(Class<T> klass) {
        return NncUtils.union(left.extractExpressions(klass), right.extractExpressions(klass));
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
}
