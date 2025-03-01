package org.metavm.compiler.syntax;

import java.util.function.Consumer;

public final class BinaryExpr extends Expr {
    private final BinOp op;
    private final Expr x;
    private final Expr y;

    public BinaryExpr(BinOp op, Expr x, Expr y) {
        this.op = op;
        this.x = x;
        this.y = y;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write(x);
        writer.write(" ");
        writer.write(op.op());
        writer.write(" ");
        writer.write(y);
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitBinaryExpr(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        action.accept(x);
        action.accept(y);
    }

    public BinOp op() {
        return op;
    }

    public Expr x() {
        return x;
    }

    public Expr y() {
        return y;
    }

    @Override
    public String toString() {
        return "BinaryExpr[" +
                "op=" + op + ", " +
                "x=" + x + ", " +
                "y=" + y + ']';
    }

}
