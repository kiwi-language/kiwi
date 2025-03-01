package org.metavm.compiler.syntax;

import java.util.function.Consumer;

public class RefExpr extends Expr {

    private Ident name;

    public RefExpr(Ident name) {
        this.name = name;
    }

    public Ident getName() {
        return name;
    }

    public void setName(Ident name) {
        this.name = name;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write(name);
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitRefExpr(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        action.accept(name);
    }
}
