package org.metavm.compiler.syntax;

import java.util.Objects;
import java.util.function.Consumer;

public final class ExprStmt extends Stmt {
    private final Expr expr;

    public ExprStmt(Expr expr) {
        this.expr = expr;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write(expr);
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitExprStmt(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        action.accept(expr);
    }

    public Expr expr() {
        return expr;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ExprStmt) obj;
        return Objects.equals(this.expr, that.expr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expr);
    }

    @Override
    public String toString() {
        return "ExprStmt[" +
                "expr=" + expr + ']';
    }

}
