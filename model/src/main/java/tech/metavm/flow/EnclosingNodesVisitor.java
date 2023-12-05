package tech.metavm.flow;

import tech.metavm.entity.Element;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.flow.NodeRT;

public abstract class EnclosingNodesVisitor<R> extends ElementVisitor<R> {

    @Override
    public final R visitElement(Element element) {
        return defaultValue();
    }

    public abstract R defaultValue();

    @Override
    public R visitNode(NodeRT<?> node) {
        var scope = node.getScope();
        if(scope.getOwner() != null)
            scope.getOwner().accept(this);
        return super.visitNode(node);
    }

}
