package tech.metavm.transpile.ir;

public class ThisIRExpression extends IRExpressionBase {

    private final IRType type;

    public ThisIRExpression(IRType type) {
        this.type = type;
    }

    @Override
    public IRType type() {
        return type;
    }
}
