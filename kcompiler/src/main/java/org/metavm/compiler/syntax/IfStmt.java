package org.metavm.compiler.syntax;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

public final class IfStmt extends Stmt {
    private final Expr cond;
    private final Stmt body;
    @Nullable
    private final Stmt else_;

    public IfStmt(
            Expr cond,
            Stmt body,
            @Nullable Stmt else_

    ) {
        this.cond = cond;
        this.body = body;
        this.else_ = else_;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write("if (");
        writer.write(cond);
        writer.write(") ");
        writer.write(body);
        if (else_ != null) {
            writer.write(" ");
            writer.write(else_);
        }
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitIfStmt(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        action.accept(cond);
        action.accept(body);
        if (else_ != null)
            action.accept(else_);
    }

    public Expr cond() {
        return cond;
    }

    public Stmt body() {
        return body;
    }

    @Nullable
    public Stmt else_() {
        return else_;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (IfStmt) obj;
        return Objects.equals(this.cond, that.cond) &&
                Objects.equals(this.body, that.body) &&
                Objects.equals(this.else_, that.else_);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cond, body, else_);
    }

    @Override
    public String toString() {
        return "IfStmt[" +
                "cond=" + cond + ", " +
                "body=" + body + ", " +
                "else_=" + else_ + ']';
    }

}
