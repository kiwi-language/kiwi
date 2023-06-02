package tech.metavm.transpile.ir;

public class SuperIRExpression extends IRExpressionBase {

    private final IRType type;

    public SuperIRExpression(IRType type) {
        this.type = type;
    }

    @Override
    public IRType type() {
        return null;
    }
}
