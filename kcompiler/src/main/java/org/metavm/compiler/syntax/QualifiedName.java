package org.metavm.compiler.syntax;

import java.util.function.Consumer;

public class QualifiedName extends Name {

    private Name qualifier;

    private Ident ident;


    public QualifiedName(Name qualifier, Ident ident) {
        this.qualifier = qualifier;
        this.ident = ident;
    }

    public Name getQualifier() {
        return qualifier;
    }

    public void setQualifier(Name qualifier) {
        this.qualifier = qualifier;
    }

    public Ident getIdent() {
        return ident;
    }

    public void setIdent(Ident ident) {
        this.ident = ident;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write(qualifier);
        writer.write(".");
        writer.write(ident);
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitQualifiedName(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        action.accept(qualifier);
        action.accept(ident);
    }
}
