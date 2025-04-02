package org.metavm.compiler.syntax;

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
    public String toString() {
        return "DoWhileStmt[" +
                "cond=" + cond + ", " +
                "body=" + body + ']';
    }

}
