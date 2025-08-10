package org.metavm.compiler.element;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.compiler.analyze.Env;
import org.metavm.compiler.generate.Code;
import org.metavm.compiler.type.Type;

import java.util.function.Consumer;

@Slf4j
public class LocalVar extends ElementBase implements Variable {

    private Name name;
    private Type type;
    private int index = -1;
    private final Executable executable;

    public LocalVar(Name name, Type type, @NotNull Executable executable) {
        this.name = name;
        this.type = type;
        this.executable = executable;
    }

    @Override
    public Name getName() {
        return name;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitLocalVariable(this);
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
    public void setName(Name name) {
        this.name = name;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public void load(Code code, Env env) {
        if (executable == code.getExecutable())
            code.load(this);
        else
            code.loadContextSlot(env.getContextIndex(executable), this);
    }

    @Override
    public void store(Code code, Env env) {
        if (executable == code.getExecutable())
            code.store(index);
        else
            code.storeContextSlot(env.getContextIndex(executable), index);
    }

    @Override
    public VariableScope getScope() {
        return executable;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Executable getExecutable() {
        return executable;
    }

    @Override
    public String toString() {
        return name.toString();
    }
}
