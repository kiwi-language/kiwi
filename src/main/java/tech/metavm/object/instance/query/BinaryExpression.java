package tech.metavm.object.instance.query;

public class BinaryExpression extends Expression {
    private final Operator operator;
    private final Expression first;
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
    public String toString() {
        return "(" + first + " " + operator + " " + second + ")";
    }
}
