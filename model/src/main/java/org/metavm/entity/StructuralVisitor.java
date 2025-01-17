package org.metavm.entity;

public abstract class StructuralVisitor<R> extends ElementVisitor<R> {

    public abstract R defaultValue(Element element);

    @Override
    public R visitElement(Element element) {
        element.acceptChildren(this);
        return defaultValue(element);
    }

}
