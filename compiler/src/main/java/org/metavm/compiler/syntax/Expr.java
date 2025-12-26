package org.metavm.compiler.syntax;

import lombok.extern.slf4j.Slf4j;
import org.metavm.compiler.element.Element;
import org.metavm.compiler.element.ValueElement;
import org.metavm.compiler.type.PrimitiveType;
import org.metavm.compiler.type.Type;

@Slf4j
public abstract class Expr extends Node {

    private ExprStatus status = ExprStatus.NONE;
    private Element element;
    private Type type = PrimitiveType.NEVER;

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
        if (element instanceof ValueElement v)
            type = v.getType();
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public ExprStatus getStatus() {
        return status;
    }

    public void setStatus(ExprStatus status) {
        this.status = status;
    }

    @Override
    public Expr setPos(int pos) {
        return (Expr) super.setPos(pos);
    }

    public boolean isMutable() {
        return false;
    }

}
