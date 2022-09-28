package tech.metavm.object.instance.query;

public class UnaryExpression extends Expression {
    private final Operator operator;
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
    public String toString() {
        if(operator.isPrefix()) {
            if(operator.toString().length() == 1) {
                return "(" + operator + operand + ")";
            }
            else {
                return "(" + operator + " " + operand + ")";
            }
        }
        else {
            return "(" + operand + " " + operator + ")";
        }
    }
}
