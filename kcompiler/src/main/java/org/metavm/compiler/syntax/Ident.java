package org.metavm.compiler.syntax;

import org.metavm.compiler.element.SymName;

import java.util.Objects;
import java.util.function.Consumer;

public class Ident extends Name {
    private final SymName value;

    public Ident(SymName value) {
        this.value = value;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write(value);
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitIdent(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {

    }

    public SymName value() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Ident) obj;
        return Objects.equals(this.value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "Name[" +
                "value=" + value + ']';
    }

}
