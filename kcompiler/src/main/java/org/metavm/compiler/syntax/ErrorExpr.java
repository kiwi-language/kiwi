package org.metavm.compiler.syntax;

import java.util.function.Consumer;

public class ErrorExpr extends Expr {

    public ErrorExpr() {
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write("<error>");
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitErrorExpr(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
    }

}
