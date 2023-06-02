package tech.metavm.transpile.ir;

public record StaticFieldExpression(
        IRField field
) implements IRExpression {
    @Override
    public IRType type() {
        return field.type();
    }
}
