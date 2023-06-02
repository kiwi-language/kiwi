package tech.metavm.transpile.ir;

public record Throw(
        IRExpression expression
) implements Statement {
}
