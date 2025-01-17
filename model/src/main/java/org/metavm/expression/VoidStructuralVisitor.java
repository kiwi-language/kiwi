package org.metavm.expression;

import org.metavm.entity.Element;
import org.metavm.entity.StructuralVisitor;
import org.metavm.object.instance.core.Instance;

public abstract class VoidStructuralVisitor extends StructuralVisitor<Void> {

    @Override
    public Void defaultValue(Element element) {
        return null;
    }

}
