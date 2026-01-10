package org.manul.compiler.element;

import org.manul.compiler.syntax.Node;
import org.manul.compiler.type.ErrorType;
import org.manul.compiler.type.Type;

import java.util.function.Consumer;

public class ErrorElement implements ValueElement {

    public static final ErrorElement instance = new ErrorElement();

    private ErrorElement() {
    }

    @Override
    public Name getName() {
        return NameTable.instance.error;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitErrorElement(this);
    }

    @Override
    public void forEachChild(Consumer<Element> action) {

    }

    @Override
    public void write(ElementWriter writer) {
        writer.write("<error>");
    }

    @Override
    public Node getNode() {
        return null;
    }

    @Override
    public void setNode(Node node) {

    }

    @Override
    public Type getType() {
        return ErrorType.instance;
    }
}
