package tech.metavm.object.type;

import tech.metavm.entity.Element;

public class VoidSuperKlassVisitor extends SuperKlassVisitor<Void> {

    @Override
    public Void visitElement(Element element) {
        return null;
    }
}
