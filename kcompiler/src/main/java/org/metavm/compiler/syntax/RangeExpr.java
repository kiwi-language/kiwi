package org.metavm.compiler.syntax;

import java.util.Objects;
import java.util.function.Consumer;

public class RangeExpr extends Expr {

    private Expr min;
    private Expr max;

    public RangeExpr(Expr min, Expr max) {
        this.min = min;
        this.max = max;
    }

    public Expr getMin() {
        return min;
    }

    public void setMin(Expr min) {
        this.min = min;
    }

    public Expr getMax() {
        return max;
    }

    public void setMax(Expr max) {
        this.max = max;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write(min);
        writer.write("...");
        writer.write(max);
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitRangeExpr(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        action.accept(min);
        action.accept(max);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        RangeExpr rangeExpr = (RangeExpr) object;
        return Objects.equals(min, rangeExpr.min) && Objects.equals(max, rangeExpr.max);
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max);
    }
}
