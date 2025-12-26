package org.metavm.compiler.element;

public class StructuralElementVisitor extends AbstractElementVisitor<Void> {
    @Override
    public Void visitElement(Element element) {
        element.forEachChild(c -> c.accept(this));
        return null;
    }
}
