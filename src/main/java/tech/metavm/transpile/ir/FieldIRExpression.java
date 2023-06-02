package tech.metavm.transpile.ir;

public class FieldIRExpression extends IRExpressionBase {

    private final IRExpression instance;
    private final IRField field;

    public FieldIRExpression(IRExpression instance, IRField field) {
        this.instance = instance;
        this.field = field;
    }

    public IRExpression getInstance() {
        return instance;
    }

    public IRField getField() {
        return field;
    }

    @Override
    public IRType type() {
        return field.type();
    }
}
