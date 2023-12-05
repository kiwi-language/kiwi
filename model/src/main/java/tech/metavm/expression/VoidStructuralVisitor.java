package tech.metavm.expression;

import tech.metavm.entity.Element;

public class VoidStructuralVisitor extends StructuralVisitor<Void> {

    @Override
    public Void defaultValue(Element element) {
        return null;
    }

}
