package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.StdKlass;

import javax.annotation.Nullable;

public class LoadTypeArgumentNode extends Node {

    private final int index;

    public LoadTypeArgumentNode(@NotNull String name, @Nullable Node previous, @NotNull Code code, int index) {
        super(name, StdKlass.type.type(), previous, code);
        this.index = index;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitLoadTypeArgumentNode(this);
    }

    @Override
    public boolean hasOutput() {
        return true;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("lt_typearg " + index);
    }

    @Override
    public int getStackChange() {
        return 0;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.LT_TYPE_ARGUMENT);
        output.writeShort(index);
    }

    @Override
    public int getLength() {
        return 3;
    }

}
