package tech.metavm.transpile.ir;

public class BinaryIRExpression extends IRExpressionBase {
    private final IRExpression first;
    private final IROperator operator;
    private final IRExpression second;

    public BinaryIRExpression(IRExpression first, IROperator operator, IRExpression second) {
        this.operator = operator;
        this.first = first;
        this.second = second;
    }

    public IROperator getOperator() {
        return operator;
    }

    public IRExpression getFirst() {
        return first;
    }

    public IRExpression getSecond() {
        return second;
    }

    @Override
    public IRType type() {
        if(operator.getType() != null) {
            return operator.getType();
        }
        else {
            return first.type();
        }
    }
}
