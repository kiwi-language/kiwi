package tech.metavm.expression;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.object.type.Type;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ValueUtil;

import java.util.List;
import java.util.Objects;

@EntityType("二元表达式")
public class BinaryExpression extends Expression {
    @EntityField(value = "运算符", asTitle = true)
    private final Operator operator;
    @ChildEntity("运算数一")
    private final Expression first;
    @ChildEntity("运算数二")
    private final Expression second;

    public BinaryExpression(Operator operator, Expression first, Expression second) {
        this.operator = operator;
        this.first = first;
        this.second = second;
    }

    public Operator getOperator() {
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
        return ValueUtil.getConvertibleType(first.getType(), second.getType());
    }

    @Override
    public List<Expression> getChildren() {
        return List.of(first, second);
    }

    @Override
    public Expression substituteChildren(List<Expression> children) {
        NncUtils.requireLength(children, 2);
        return new BinaryExpression(operator, children.get(0), children.get(1));
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
