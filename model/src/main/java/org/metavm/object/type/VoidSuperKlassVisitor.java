package org.metavm.object.type;

import org.metavm.entity.Element;

public class VoidSuperKlassVisitor extends SuperKlassVisitor<Void> {

    @Override
    public Void visitElement(Element element) {
        return null;
    }
}
