package org.metavm.compiler.element;

import org.metavm.compiler.syntax.Node;

import java.util.function.Consumer;

public interface Element {

    Name getName();

    <R> R accept(ElementVisitor<R> visitor);

    void forEachChild(Consumer<Element> action);

    void write(ElementWriter writer);

    Node getNode();

    void setNode(Node node);

    default String getText() {
        var w = new ElementWriter();
        write(w);
        return w.toString();
    }

}
