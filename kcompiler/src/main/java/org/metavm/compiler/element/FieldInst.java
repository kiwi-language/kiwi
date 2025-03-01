package org.metavm.compiler.element;

import org.metavm.compiler.generate.Code;
import org.metavm.compiler.type.ClassType;
import org.metavm.compiler.type.FunctionType;
import org.metavm.compiler.type.Type;

import java.util.function.Consumer;

public record FieldInst(ClassType declaringType, Field field, Type type) implements FieldRef {
    @Override
    public SymName getName() {
        return field.getName();
    }

    @Override
    public void invoke(Code code) {
        load(code);
        code.call((FunctionType) getType());
    }

    @Override
    public boolean isStatic() {
        return field.isStatic();
    }

    @Override
    public ClassType getDeclaringType() {
        return declaringType;
    }

    @Override
    public void setName(SymName name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Element getElement() {
        return field;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitFieldInst(this);
    }

    @Override
    public void forEachChild(Consumer<Element> action) {
    }

    @Override
    public void write(ElementWriter writer) {
        writer.writeType(declaringType);
        writer.write(".");
        writer.write(field.getName());
        writer.write(": ");
        writer.writeType(type);
    }

    @Override
    public String toString() {
        return "FieldInst " + getText();
    }

}
