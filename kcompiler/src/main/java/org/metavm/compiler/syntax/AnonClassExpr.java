package org.metavm.compiler.syntax;

import java.util.function.Consumer;

public class AnonClassExpr extends Expr {
    private ClassDecl decl;

    public AnonClassExpr(ClassDecl decl) {
        this.decl = decl;
    }

    public ClassDecl getDecl() {
        return decl;
    }

    public void setDecl(ClassDecl decl) {
        this.decl = decl;
    }

    @Override
    public void write(SyntaxWriter writer) {
        decl.writeExtends(writer);
        decl.writeBody(writer);
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitAnonClassExpr(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        action.accept(decl);
    }
}
