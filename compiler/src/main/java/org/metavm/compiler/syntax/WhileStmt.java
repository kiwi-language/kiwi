package org.metavm.compiler.syntax;

import java.util.Objects;
import java.util.function.Consumer;

public final class WhileStmt extends Stmt {
    private final Expr cond;
    private final Stmt body;

    public WhileStmt(Expr cond, Stmt body) {
        this.cond = cond;
        this.body = body;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write("while (");
        writer.write(cond);
        writer.write(") ");
        writer.write(body);
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitWhileStmt(this);
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
        WhileStmt whileStmt = (WhileStmt) object;
        return Objects.equals(cond, whileStmt.cond) && Objects.equals(body, whileStmt.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cond, body);
    }
}
