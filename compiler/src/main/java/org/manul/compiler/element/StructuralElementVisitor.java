package org.manul.compiler.element;

public class StructuralElementVisitor extends AbstractElementVisitor<Void> {
    @Override
    public Void visitElement(Element element) {
        element.forEachChild(c -> c.accept(this));
        return null;
    }
}
