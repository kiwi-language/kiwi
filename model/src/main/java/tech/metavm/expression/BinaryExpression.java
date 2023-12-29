package tech.metavm.expression;

import tech.metavm.entity.*;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.Type;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ValueUtil;

import java.util.List;
import java.util.Objects;

@EntityType("二元表达式")
public class BinaryExpression extends Expression {
    @EntityField("运算符")
    private final BinaryOperator operator;
    @ChildEntity("运算数一")
    private final Expression first;
    @ChildEntity("运算数二")
    private final Expression second;

    public BinaryExpression(BinaryOperator operator, Expression first, Expression second) {
        this.operator = operator;
        this.first = addChild(first.copy(), "first");
        this.second = addChild(second.copy(), "second");
    }

    public BinaryOperator getOperator() {
        return operator;
    }

    public Expression getFirst() {
        return first;
    }

    public Expression getSecond() {
        return second;
    }

    @Override
    public String buildSelf(VarType symbolType) {
        String firstExpr = first.build(symbolType, first.precedence() > precedence());
        String secondExpr = second.build(symbolType, second.precedence() >= precedence());
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
        return ValueUtil.getCompatibleType(first.getType(), second.getType());
    }

    @Override
    public List<Expression> getChildren() {
        return List.of(first, second);
    }

    @Override
    public Instance evaluate(EvaluationContext context) {
        return operator.evaluate(first.evaluate(context), second.evaluate(context));
    }

    @Override
    public <T extends Expression> List<T> extractExpressionsRecursively(Class<T> klass) {
        return NncUtils.union(first.extractExpressions(klass), second.extractExpressions(klass));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BinaryExpression that)) return false;
        return operator == that.operator && Objects.equals(first, that.first) && Objects.equals(second, that.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operator, first, second);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitBinaryExpression(this);
    }
}
