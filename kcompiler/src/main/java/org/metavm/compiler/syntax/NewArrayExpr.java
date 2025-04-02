package org.metavm.compiler.syntax;

import org.metavm.compiler.util.List;

import java.util.function.Consumer;

public final class NewArrayExpr extends Expr {
    private final TypeNode elementType;
    private final boolean readonly;
    private List<Expr> elements;

    public NewArrayExpr(TypeNode elementType, boolean readonly, List<Expr> elements) {
        this.elementType = elementType;
        this.readonly = readonly;
        this.elements = elements;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write(elementType);
        writer.write(readonly ? "[r]" : "[]");
        if (elements.nonEmpty()) {
            writer.write("{");
            writer.write(elements, ",");
            writer.write("}");
        }
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitNewArrayExpr(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        action.accept(elementType);
        elements.forEach(action);
    }

    public TypeNode elementType() {
        return elementType;
    }

    public boolean readonly() {
        return readonly;
    }

    public List<Expr> getElements() {
        return elements;
    }

    public void setElements(List<Expr> elements) {
        this.elements = elements;
    }

    @Override
    public String toString() {
        return "NewArray[" +
                "elementType=" + elementType + ", " +
                "readonly=" + readonly + ']';
    }

}
