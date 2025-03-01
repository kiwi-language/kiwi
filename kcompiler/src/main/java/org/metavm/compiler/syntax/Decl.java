package org.metavm.compiler.syntax;

import org.metavm.compiler.element.Element;

public abstract class Decl<T extends Element> extends Node {

    private T element;

    public T getElement() {
        return element;
    }

    public void setElement(T element) {
        this.element = element;
    }
}
