package org.metavm.compiler.element;

import org.metavm.compiler.syntax.Node;
import org.metavm.compiler.type.ErrorType;
import org.metavm.compiler.type.Type;

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
