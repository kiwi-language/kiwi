package org.metavm.compiler.syntax;

import java.util.Objects;
import java.util.function.Consumer;

public class CondExpr extends Expr {

    private Expr cond;
    private Expr truePart;
    private Expr falsePart;

    public CondExpr(Expr cond, Expr truePart, Expr falsePart) {
        this.cond = cond;
        this.truePart = truePart;
        this.falsePart = falsePart;
    }

    public Expr getCond() {
        return cond;
    }

    public void setCond(Expr cond) {
        this.cond = cond;
    }

    public Expr getTruePart() {
        return truePart;
    }

    public void setTruePart(Expr truePart) {
        this.truePart = truePart;
    }

    public Expr getFalsePart() {
        return falsePart;
    }

    public void setFalsePart(Expr falsePart) {
        this.falsePart = falsePart;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write(cond);
        writer.write(" ? ");
        writer.write(truePart);
        writer.write(" : ");
        writer.write(falsePart);
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitCondExpr(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        action.accept(cond);
        action.accept(truePart);
        action.accept(falsePart);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        CondExpr condExpr = (CondExpr) object;
        return Objects.equals(cond, condExpr.cond) && Objects.equals(truePart, condExpr.truePart) && Objects.equals(falsePart, condExpr.falsePart);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cond, truePart, falsePart);
    }
}
