package org.metavm.compiler.syntax;

import org.metavm.compiler.element.Element;
import org.metavm.compiler.element.Name;
import org.metavm.compiler.element.Variable;
import org.metavm.compiler.util.CompilationException;
import org.metavm.compiler.util.List;

import java.util.Objects;
import java.util.function.Consumer;

public class Ident extends Expr {

    public static Ident from(String name) {
        return new Ident(Name.from(name));
    }

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

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Ident ident = (Ident) object;
        return Objects.equals(name, ident.name) && Objects.equals(candidates, ident.candidates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, candidates);
    }

    @Override
    public boolean isMutable() {
        var e = getElement();
        if (e == null)
            throw new CompilationException("Expression not yet attributed: " + getText());
        return e instanceof Variable v && v.isMutable();
    }
}
