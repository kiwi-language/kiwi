package org.metavm.entity;

import org.metavm.util.LinkedList;

import javax.annotation.Nullable;

public abstract class StructuralVisitor<R> extends ElementVisitor<R> {

    public abstract R defaultValue(Element element);

    private final LinkedList<EntityParentRef> parentRefs = new LinkedList<>();

    @Override
    public R visitElement(Element element) {
        element.acceptChildren(this);
        return defaultValue(element);
    }

    public final void pushParentRef(EntityParentRef parentRef) {
        parentRefs.push(parentRef);
    }

    public final void popParentRef() {
        parentRefs.pop();
    }

    public final @Nullable EntityParentRef parentRef() {
        return parentRefs.peek();
    }

}
