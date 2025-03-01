package org.metavm.compiler.element;

import java.util.function.Consumer;

public interface Element {

    SymName getName();

    <R> R accept(ElementVisitor<R> visitor);

    void forEachChild(Consumer<Element> action);

    void write(ElementWriter writer);

    default String getText() {
        var w = new ElementWriter();
        write(w);
        return w.toString();
    }

}
