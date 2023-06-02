package tech.metavm.transpile.ir;

public record LocalTypeDeclaration(
        IRType type
) implements Statement {
}
