package org.metavm.compiler.syntax;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

public final class AssignExpr extends Expr {
    private final @Nullable BinOp op;
    private final Expr lhs;
    private final Expr rhs;

    public AssignExpr(@Nullable BinOp op, Expr lhs, Expr rhs) {
        this.op = op;
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write(lhs);
        writer.write(" ");
        if (op == null) writer.write("=");
        else writer.write(op.op());
        writer.write(" ");
        writer.write(rhs);
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitAssignExpr(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        action.accept(lhs);
        action.accept(rhs);
    }

    public @Nullable BinOp op() {
        return op;
    }

    public Expr lhs() {
        return lhs;
    }

    public Expr rhs() {
        return rhs;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (AssignExpr) obj;
        return Objects.equals(this.op, that.op) &&
                Objects.equals(this.lhs, that.lhs) &&
                Objects.equals(this.rhs, that.rhs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(op, lhs, rhs);
    }

}
