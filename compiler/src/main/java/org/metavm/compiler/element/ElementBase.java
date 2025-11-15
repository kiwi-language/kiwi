package org.metavm.compiler.element;

import org.metavm.compiler.syntax.Node;

public abstract class ElementBase implements Element{

    private Node node;

    @Override
    public Node getNode() {
        return node;
    }

    @Override
    public void setNode(Node node) {
        this.node = node;
    }
}
