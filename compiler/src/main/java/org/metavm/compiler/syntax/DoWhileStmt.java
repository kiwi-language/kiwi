package org.metavm.compiler.syntax;

import java.util.Objects;
import java.util.function.Consumer;

public final class DoWhileStmt extends Stmt {
    private final Expr cond;
    private final Stmt body;

    public DoWhileStmt(Expr cond, Stmt body) {
        this.cond = cond;
        this.body = body;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write("do ");
        writer.write(body);
        writer.write(" while (");
        writer.write(cond);
        writer.write(") ");
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitDoWhileStmt(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        action.accept(cond);
        action.accept(body);
    }

    public Expr cond() {
        return cond;
    }

    public Stmt body() {
        return body;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        DoWhileStmt that = (DoWhileStmt) object;
        return Objects.equals(cond, that.cond) && Objects.equals(body, that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cond, body);
    }
}
