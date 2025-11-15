package org.metavm.compiler.syntax;

import java.util.Objects;
import java.util.function.Consumer;

public final class BinaryExpr extends Expr {
    private final BinOp op;
    private final Expr lhs;
    private final Expr rhs;

    public BinaryExpr(BinOp op, Expr lhs, Expr rhs) {
        this.op = op;
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write(lhs);
        writer.write(" ");
        writer.write(op.op());
        writer.write(" ");
        writer.write(rhs);
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitBinaryExpr(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        action.accept(lhs);
        action.accept(rhs);
    }

    public BinOp op() {
        return op;
    }

    public Expr lhs() {
        return lhs;
    }

    public Expr rhs() {
        return rhs;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        BinaryExpr that = (BinaryExpr) object;
        return op == that.op && Objects.equals(lhs, that.lhs) && Objects.equals(rhs, that.rhs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(op, lhs, rhs);
    }
}
