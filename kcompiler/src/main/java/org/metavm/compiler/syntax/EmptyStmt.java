package org.metavm.compiler.syntax;

import java.util.function.Consumer;

public final class EmptyStmt extends Stmt {
    public EmptyStmt() {
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write(";");
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitEmptyStmt(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {

    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj != null && obj.getClass() == this.getClass();
    }

    @Override
    public int hashCode() {
        return 1;
    }

}
