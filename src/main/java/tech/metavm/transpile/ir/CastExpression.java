package tech.metavm.transpile.ir;

public record CastExpression(
        IRExpression expression,
        IRType type
) implements IRExpression {
}
