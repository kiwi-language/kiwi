package org.metavm.compiler.syntax;

import java.util.Objects;
import java.util.function.Consumer;

public final class IndexExpr extends Expr {
    private final Expr x;
    private final Expr index;

    public IndexExpr(Expr x, Expr index) {
        this.x = x;
        this.index = index;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write(x);
        writer.write("[");
        writer.write(index);
        writer.write("]");
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitIndexExpr(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        action.accept(x);
        action.accept(index);
    }

    public Expr x() {
        return x;
    }

    public Expr index() {
        return index;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (IndexExpr) obj;
        return Objects.equals(this.x, that.x) &&
                Objects.equals(this.index, that.index);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, index);
    }

    @Override
    public boolean isMutable() {
        return true;
    }
}