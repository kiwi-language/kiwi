package tech.metavm.entity;

import tech.metavm.expression.StructuralVisitor;

public class StructuralTransformer extends StructuralVisitor<Element> {

    @Override
    public Element defaultValue(Element element) {
        return element;
    }
}
