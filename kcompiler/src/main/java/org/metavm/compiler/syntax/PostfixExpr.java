package org.metavm.compiler.syntax;

import java.util.function.Consumer;

public final class PostfixExpr extends Expr {
    private final PostfixOp op;
    private final Expr x;

    public PostfixExpr(PostfixOp op, Expr x) {
        this.op = op;
        this.x = x;
    }

    @Override
    public void write(SyntaxWriter writer) {
        x.write(writer);
        writer.write(op.op());
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitPostfixExpr(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        action.accept(x);
    }

    public PostfixOp op() {
        return op;
    }

    public Expr x() {
        return x;
    }

}
