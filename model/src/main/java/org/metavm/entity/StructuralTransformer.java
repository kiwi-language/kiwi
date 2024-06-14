package org.metavm.entity;

public class StructuralTransformer extends StructuralVisitor<Element> {

    @Override
    public Element defaultValue(Element element) {
        return element;
    }
}
