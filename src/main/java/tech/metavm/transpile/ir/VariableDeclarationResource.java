package tech.metavm.transpile.ir;

public record VariableDeclarationResource(
        LocalVariable variable,
        IRExpression expression
) implements Resource {
}
