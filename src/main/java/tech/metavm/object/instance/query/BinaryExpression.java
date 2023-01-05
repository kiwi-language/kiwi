package tech.metavm.object.instance.query;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.ValueType;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ValueUtil;

import java.util.List;

@EntityType("二元表达式")
public class BinaryExpression extends Expression {
    @EntityField(value = "运算符", asTitle = true)
    private final Operator operator;
    @EntityField("运算数一")
    private final Expression first;
    @EntityField("运算数二")
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
    public <T extends Expression> List<T> extractExpressionsRecursively(Class<T> klass) {
        return NncUtils.merge(first.extractExpressions(klass), second.extractExpressions(klass));
    }
}
