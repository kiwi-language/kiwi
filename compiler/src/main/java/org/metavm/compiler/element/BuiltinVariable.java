package org.metavm.compiler.element;

import org.metavm.compiler.analyze.Env;
import org.metavm.compiler.generate.Code;
import org.metavm.compiler.type.Type;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public final class BuiltinVariable extends ElementBase implements Variable {

    private final VariableScope scope;
    private final Name name;
    private final Element element;
    private Type type;

    public BuiltinVariable(VariableScope scope, Name name, @Nullable Element element, Type type) {
        this.scope = scope;
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
    public Name getName() {
        return name;
    }

    @Override
    public void setName(Name name) {
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
    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public void load(Code code, Env env) {
        code.loadThis(env);
    }

    @Override
    public void store(Code code, Env env) {

    }

    @Override
    public VariableScope getScope() {
        return scope;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public String toString() {
        return  name + ": " + type.getTypeText();
    }
}
