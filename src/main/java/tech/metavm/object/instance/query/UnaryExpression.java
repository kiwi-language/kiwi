package tech.metavm.object.instance.query;

import tech.metavm.object.meta.Type;

import java.util.List;

public class UnaryExpression extends Expression {
    private final Operator operator;
    private final Expression operand;

    public UnaryExpression(Operator operator, Expression operand) {
        super(operand.context);
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
        if(operator.resultType(context) != null) {
            return operator.resultType(context);
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
