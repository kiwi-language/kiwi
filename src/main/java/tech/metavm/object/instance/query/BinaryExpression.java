package tech.metavm.object.instance.query;

import tech.metavm.entity.ValueType;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ValueUtil;

import java.util.List;

@ValueType("二元表达式")
public class BinaryExpression extends Expression {
    private final Operator operator;
    private final Expression first;
    private final Expression second;

    public BinaryExpression(Operator operator, Expression first, Expression second) {
//        super(first.context);
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
