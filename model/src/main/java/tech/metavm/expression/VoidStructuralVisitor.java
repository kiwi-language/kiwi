package tech.metavm.expression;

import tech.metavm.entity.Element;
import tech.metavm.entity.StructuralVisitor;

public class VoidStructuralVisitor extends StructuralVisitor<Void> {

    @Override
    public Void defaultValue(Element element) {
        return null;
    }

}
