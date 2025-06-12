package org.metavm.compiler.syntax;

import org.metavm.util.Utils;

import java.util.Objects;
import java.util.function.Consumer;

public final class Literal extends Expr {
    private final Object value;

    public Literal(Object value) {
        this.value = value;
    }

    @Override
    public void write(SyntaxWriter writer) {
        String text;
        if (value instanceof String s)
            text = "\"" + Utils.escape(s) + "\"";
        else
            text = Objects.toString(value);
        writer.write(text);
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitLiteral(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {

    }

    public Object value() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Literal) obj;
        return Objects.equals(this.value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

}
