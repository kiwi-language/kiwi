package org.metavm.compiler.syntax;

import java.util.function.Consumer;

public final class SelectorExpr extends Expr {
    private final Expr x;
    private final Ident sel;

    public SelectorExpr(Expr x, Ident sel) {
        this.x = x;
        this.sel = sel;
    }

    @Override
    public void write(SyntaxWriter writer) {
        x.write(writer);
        writer.write(".");
        sel.write(writer);
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitSelectorExpr(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        action.accept(x);
        action.accept(sel);
    }

    public Expr x() {
        return x;
    }

    public Ident sel() {
        return sel;
    }

}
