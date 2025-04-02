package org.metavm.compiler.syntax;

import org.metavm.compiler.element.Element;
import org.metavm.compiler.element.Name;
import org.metavm.compiler.util.List;

import java.util.function.Consumer;

public class Ident extends Expr {

    private Name name;

    private Iterable<Element> candidates = List.nil();

    public Ident(Name name) {
        this.name = name;
    }

    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

    public Iterable<Element> getCandidates() {
        return candidates;
    }

    public void setCandidates(Iterable<Element> candidates) {
        this.candidates = candidates;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write(name);
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitIdent(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
    }
}
