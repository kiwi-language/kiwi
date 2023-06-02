package tech.metavm.transpile.ir;

public record VariableDeclarationForInit(
        LocalVariableDeclaration variableDeclaration
) implements ForInit {
}
