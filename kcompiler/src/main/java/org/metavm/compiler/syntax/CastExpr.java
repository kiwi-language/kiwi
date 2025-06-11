package org.metavm.compiler.syntax;

import java.util.Objects;
import java.util.function.Consumer;

public final class CastExpr extends Expr {
    private final TypeNode type;
    private final Expr expr;

    public CastExpr(TypeNode type, Expr expr) {
        this.type = type;
        this.expr = expr;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write("(");
        writer.write(type);
        writer.write(") ");
        writer.write(expr);
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitCastExpr(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        action.accept(type);
        action.accept(expr);
    }

    public TypeNode type() {
        return type;
    }

    public Expr expr() {
        return expr;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (CastExpr) obj;
        return Objects.equals(this.type, that.type) &&
                Objects.equals(this.expr, that.expr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, expr);
    }

}
