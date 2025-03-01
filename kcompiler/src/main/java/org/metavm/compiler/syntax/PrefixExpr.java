package org.metavm.compiler.syntax;

import java.util.function.Consumer;

public final class PrefixExpr extends Expr {
    private final PrefixOp op;
    private final Expr x;

    public PrefixExpr(PrefixOp op, Expr x) {
        this.op = op;
        this.x = x;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write(op.op());
        x.write(writer);
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitPrefixExpr(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        action.accept(x);
    }

    public PrefixOp op() {
        return op;
    }

    public Expr x() {
        return x;
    }

}
