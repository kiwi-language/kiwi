package org.metavm.compiler.element;

import org.metavm.compiler.analyze.Env;
import org.metavm.compiler.generate.Code;
import org.metavm.compiler.type.ClassType;
import org.metavm.compiler.type.FuncType;
import org.metavm.compiler.type.Type;

import java.util.function.Consumer;

public final class FieldInst extends ElementBase implements FieldRef {
    private final ClassType declaringType;
    private final Field field;
    private Type type;

    public FieldInst(ClassType declaringType, Field field, Type type) {
        this.declaringType = declaringType;
        this.field = field;
        this.type = type;
        field.addInstance(this);
    }

    @Override
    public Name getName() {
        return field.getName();
    }

    @Override
    public void invoke(Code code, Env env) {
        load(code, env);
        code.call((FuncType) getType());
    }

    @Override
    public boolean isStatic() {
        return field.isStatic();
    }

    @Override
    public ClassType getDeclType() {
        return declaringType;
    }

    @Override
    public void setName(Name name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public void setType(Type type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VariableScope getScope() {
        return declaringType;
    }

    @Override
    public Field getElement() {
        return field;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitFieldInst(this);
    }

    @Override
    public void forEachChild(Consumer<Element> action) {
    }

    public void onFieldTypeChange() {
        type = field.getType().accept(declaringType.getSubstitutor());
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

    public Field field() {
        return field;
    }

    public Type type() {
        return type;
    }

}
