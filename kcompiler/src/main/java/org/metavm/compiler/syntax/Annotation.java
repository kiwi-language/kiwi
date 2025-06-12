package org.metavm.compiler.syntax;

import org.metavm.compiler.element.Name;
import org.metavm.compiler.util.CompilationException;
import org.metavm.compiler.util.List;

import java.util.Objects;
import java.util.function.Consumer;

public class Annotation extends Node {

    private Name name;
    private List<Attribute> attributes;

    public Annotation(Name name, List<Attribute> attributes) {
        this.name = name;
        this.attributes = attributes;
    }

    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write("@");
        writer.write(name);
        if (!attributes.isEmpty()) {
            writer.write("(");
            writer.write(attributes);
            writer.write(")");
        }
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitAnnotation(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        attributes.forEach(action);
    }

    public Object extractValue() {
        if (getAttributes().size() != 1)
            throw new  CompilationException("Invalid annotation: " + this);
        return getAttributes().getFirst().getLiteralValue();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Annotation that = (Annotation) object;
        return Objects.equals(name, that.name) && Objects.equals(attributes, that.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, attributes);
    }

    public static class Attribute extends Node {
        private Name name;
        private Expr value;

        public Attribute(Name name, Expr value) {
            this.name = name;
            this.value = value;
        }

        public Name getName() {
            return name;
        }

        public void setName(Name name) {
            this.name = name;
        }

        public Expr getValue() {
            return value;
        }

        public Object getLiteralValue() {
            if (value instanceof Literal literal)
                return literal.value();
            else
                throw new CompilationException("Annotation attribute '"
                        + name + "=" + value.getText() + "'"
                        + " must have a literal value"
                );
        }

        public void setValue(Expr value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;
            Attribute attribute = (Attribute) object;
            return Objects.equals(name, attribute.name) && Objects.equals(value, attribute.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, value);
        }

        @Override
        public void write(SyntaxWriter writer) {
            writer.write(name);
            writer.write(" = ");
            writer.write(value);
        }

        @Override
        public <R> R accept(NodeVisitor<R> visitor) {
            return visitor.visitAttribute(this);
        }

        @Override
        public void forEachChild(Consumer<Node> action) {

        }
    }

}
