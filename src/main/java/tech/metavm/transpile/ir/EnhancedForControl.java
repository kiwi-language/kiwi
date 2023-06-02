package tech.metavm.transpile.ir;

public record EnhancedForControl(
    LocalVariable variable,
    IRExpression expression
) implements ForControl{
}
