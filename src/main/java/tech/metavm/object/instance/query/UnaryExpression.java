package tech.metavm.object.instance.query;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.ValueType;
import tech.metavm.object.meta.Type;

import java.util.List;

@EntityType("一元表达式")
public class UnaryExpression extends Expression {
    @EntityField("运算符")
    private final Operator operator;
    @EntityField("运算数")
    private final Expression operand;

    public UnaryExpression(Operator operator, Expression operand) {
        this.operator = operator;
        this.operand = operand;
    }

    public Operator getOperator() {
        return operator;
    }

    public Expression getOperand() {
        return operand;
    }

    @Override
    public String buildSelf(VarType symbolType) {
        boolean operandParenthesized = operand.precedence() >= precedence();
        String operandExpr = operand.build(symbolType, operandParenthesized);
        if(operator.isPrefix()) {
            return operator + " " + operandExpr;
        }
        else {
            return operandExpr + " " + operator;
        }
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
        else {
            return operand.getType();
        }
    }

    @Override
    protected <T extends Expression> List<T> extractExpressionsRecursively(Class<T> klass) {
        return operand.extractExpressions(klass);
    }
}
