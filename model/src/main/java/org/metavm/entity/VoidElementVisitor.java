package org.metavm.entity;

public class VoidElementVisitor extends ElementVisitor<Void> {
    @Override
    public Void visitElement(Element element) {
        return null;
    }
}
