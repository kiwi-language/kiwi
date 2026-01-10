package org.manul.expression;

import org.manul.entity.Element;
import org.manul.entity.StructuralVisitor;
import org.manul.object.instance.core.Instance;

public abstract class VoidStructuralVisitor extends StructuralVisitor<Void> {

    @Override
    public Void defaultValue(Element element) {
        return null;
    }

}
