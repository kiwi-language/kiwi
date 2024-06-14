package org.metavm.expression;

import org.metavm.entity.Element;
import org.metavm.entity.StructuralVisitor;

public class VoidStructuralVisitor extends StructuralVisitor<Void> {

    @Override
    public Void defaultValue(Element element) {
        return null;
    }

}
