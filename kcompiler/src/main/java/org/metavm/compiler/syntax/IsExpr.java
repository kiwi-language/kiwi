package org.metavm.compiler.syntax;

import java.util.Objects;
import java.util.function.Consumer;

public final class IsExpr extends Expr {
    private final Expr expr;
    private final TypeNode checkType;

    public IsExpr(Expr expr, TypeNode checkType) {
        this.expr = expr;
        this.checkType = checkType;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write(expr);
        writer.write(" instanceof ");
        writer.write(checkType);
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitIsExpr(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        action.accept(expr);
        action.accept(checkType);
    }

    public Expr getExpr() {
        return expr;
    }

    public TypeNode getCheckType() {
        return checkType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (IsExpr) obj;
        return Objects.equals(this.expr, that.expr) &&
                Objects.equals(this.checkType, that.checkType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expr, checkType);
    }

    @Override
    public String toString() {
        return "InstanceOfExpr[" +
                "expr=" + expr + ", " +
                "type=" + checkType + ']';
    }

}
