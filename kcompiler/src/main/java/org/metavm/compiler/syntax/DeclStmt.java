package org.metavm.compiler.syntax;

import java.util.Objects;
import java.util.function.Consumer;

public class DeclStmt extends Stmt {

    private Decl<?> decl;

    public DeclStmt(Decl<?> decl) {
        this.decl = decl;
    }

    public Decl<?> getDecl() {
        return decl;
    }

    public void setDecl(Decl<?> decl) {
        this.decl = decl;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write(decl);
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitDeclStmt(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        action.accept(decl);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        DeclStmt declStmt = (DeclStmt) object;
        return Objects.equals(decl, declStmt.decl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(decl);
    }
}
