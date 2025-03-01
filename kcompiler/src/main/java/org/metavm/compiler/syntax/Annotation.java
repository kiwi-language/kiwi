package org.metavm.compiler.syntax;

import org.metavm.compiler.element.SymName;
import org.metavm.compiler.util.List;

import java.util.function.Consumer;

public class Annotation extends Node {

    private SymName name;
    private List<Attribute> attributes;

    public Annotation(SymName name, List<Attribute> attributes) {
        this.name = name;
        this.attributes = attributes;
    }

    public SymName getName() {
        return name;
    }

    public void setName(SymName name) {
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
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitAnnotation(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
    }

    public static class Attribute {
        private SymName name;
        private Expr value;

        public Attribute(SymName name, Expr value) {
            this.name = name;
            this.value = value;
        }

        public SymName getName() {
            return name;
        }

        public void setName(SymName name) {
            this.name = name;
        }

        public Expr getValue() {
            return value;
        }

        public void setValue(Expr value) {
            this.value = value;
        }
    }

}
