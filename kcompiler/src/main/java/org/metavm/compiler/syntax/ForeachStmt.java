package org.metavm.compiler.syntax;

import java.util.Objects;
import java.util.function.Consumer;

public class ForeachStmt extends Stmt {

    private LocalVarDecl var;
    private Expr expr;
    private Stmt body;

    public ForeachStmt(LocalVarDecl var, Expr expr, Stmt body) {
        this.var = var;
        this.expr = expr;
        this.body = body;
    }

    public LocalVarDecl getVar() {
        return var;
    }

    public void setVar(LocalVarDecl var) {
        this.var = var;
    }

    public Expr getExpr() {
        return expr;
    }

    public void setExpr(Expr expr) {
        this.expr = expr;
    }

    public Stmt getBody() {
        return body;
    }

    public void setBody(Stmt body) {
        this.body = body;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write("for (");
        writer.write(var.getName());
        writer.write(" in ");
        writer.write(expr);
        writer.write(") ");
        writer.write(body);
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitForeachStmt(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        action.accept(var);
        action.accept(expr);
        action.accept(body);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        ForeachStmt that = (ForeachStmt) object;
        return Objects.equals(var, that.var) && Objects.equals(expr, that.expr) && Objects.equals(body, that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(var, expr, body);
    }
}
