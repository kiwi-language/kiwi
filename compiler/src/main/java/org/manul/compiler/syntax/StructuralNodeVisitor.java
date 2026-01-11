package org.manul.compiler.syntax;

public class StructuralNodeVisitor extends AbstractNodeVisitor<Void> {

    public Void visitNode(Node node) {
        node.forEachChild(c -> c.accept(this));
        return null;
    }

}
