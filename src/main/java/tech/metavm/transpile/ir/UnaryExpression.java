package tech.metavm.transpile.ir;

public record UnaryExpression(IRExpression operand, IROperator operator) implements IRExpression {

    @Override
    public IRType type() {
        return null;
    }
}
