package tech.metavm.transpile.ir;

public class LocalVariableIRExpression extends IRExpressionBase {

    private final LocalVariable variable;

    public LocalVariableIRExpression(LocalVariable variable) {
        this.variable = variable;
    }

    @Override
    public IRType type() {
        return variable.type();
    }
}
