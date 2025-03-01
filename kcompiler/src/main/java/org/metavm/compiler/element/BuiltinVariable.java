package org.metavm.compiler.element;

import org.metavm.compiler.generate.Code;
import org.metavm.compiler.type.Type;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public final class BuiltinVariable implements Variable {

    private final SymName name;
    private final Element element;
    private final Type type;

    public BuiltinVariable(SymName name, @Nullable Element element,  Type type) {
        this.name = name;
        this.element = element;
        this.type = type;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitBuiltinVariable(this);
    }

    @Override
    public void forEachChild(Consumer<Element> action) {

    }

    @Override
    public void write(ElementWriter writer) {
        writer.write(name);
        writer.write(": ");
        writer.writeType(type);
    }

    @Override
    public SymName getName() {
        return name;
    }

    @Override
    public void setName(SymName name) {
        throw new UnsupportedOperationException();
    }

    public Element getElement() {
        return element;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public void load(Code code) {
        code.loadThis();
    }

    @Override
    public void store(Code code) {

    }

    @Override
    public String toString() {
        return  name + ": " + type.getText();
    }
}
