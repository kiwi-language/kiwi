package org.manul.compiler.syntax;

import org.manul.compiler.element.Element;

public abstract class Decl<T extends Element> extends Node {

    private T element;

    public T getElement() {
        return element;
    }

    public void setElement(T element) {
        element.setNode(this);
        this.element = element;
    }
}
