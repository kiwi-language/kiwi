package org.metavm.compiler.element;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.compiler.generate.Code;
import org.metavm.compiler.type.Type;

import java.util.function.Consumer;

@Slf4j
public class LocalVariable implements Variable {

    private SymName name;
    private Type type;
    private int index = -1;
    private final Executable executable;

    public LocalVariable(SymName name, Type type, @NotNull Executable executable) {
        this.name = name;
        this.type = type;
        this.executable = executable;
    }

    @Override
    public SymName getName() {
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
    public void setName(SymName name) {
        this.name = name;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public void load(Code code) {
        if (getExecutable() == code.getExecutable())
            code.load(this);
        else
            code.loadContextSlot(getContextIndex(code), this);
    }

    @Override
    public void store(Code code) {
        if (getExecutable() == code.getExecutable())
            code.store(index);
        else
            code.storeContextSlot(getContextIndex(code), index);
    }

    private int getContextIndex(Code code) {
        var target = getExecutable();
        var idx = -1;
        Element e = code.getExecutable();
        while (e != null && e != target) {
            e = switch (e) {
                case Lambda lambda -> lambda.getEnclosing();
                case Method method -> method.getDeclaringClass();
                case Clazz clazz -> (Element) clazz.getScope();
                default -> null;
            };
            if (e instanceof Executable)
                idx++;
        }
        if (e == null)
            throw new RuntimeException("Not enclosed by: " + target);
        return idx;
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
}
