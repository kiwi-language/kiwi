package org.metavm.compiler.syntax;

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
    public String toString() {
        return "BinaryExpr[" +
                "op=" + op + ", " +
                "lhs=" + lhs + ", " +
                "rhs=" + rhs + ']';
    }

}
