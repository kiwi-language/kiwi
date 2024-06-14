package org.metavm.flow;

import org.metavm.entity.Element;
import org.metavm.entity.ElementVisitor;

public abstract class EnclosingNodesVisitor<R> extends ElementVisitor<R> {

    @Override
    public final R visitElement(Element element) {
        return defaultValue();
    }

    public abstract R defaultValue();

    @Override
    public R visitNode(NodeRT node) {
        var scope = node.getScope();
        if(scope.getOwner() != null)
            return scope.getOwner().accept(this);
        else
            return super.visitNode(node);
    }

}
