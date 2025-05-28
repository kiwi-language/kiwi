package org.metavm.compiler.syntax;

import java.util.function.Consumer;

public class DelStmt extends Stmt {

    private Expr expr;

    public DelStmt(Expr expr) {
        this.expr = expr;
    }

    public Expr getExpr() {
        return expr;
    }

    public void setExpr(Expr expr) {
        this.expr = expr;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write("delete ");
        writer.write(expr);
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitDelStmt(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        action.accept(expr);
    }
}
