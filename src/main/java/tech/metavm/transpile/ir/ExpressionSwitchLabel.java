package tech.metavm.transpile.ir;

public record ExpressionSwitchLabel(
        IRExpression expression
) implements SwitchLabel{
}
