package org.manul.object.type;

import org.manul.entity.Element;

public class VoidSuperKlassVisitor extends SuperKlassVisitor<Void> {

    @Override
    public Void visitElement(Element element) {
        return null;
    }
}
