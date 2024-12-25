package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.StdKlass;

import javax.annotation.Nullable;

public class LoadArrayTypeNode extends Node {

    public LoadArrayTypeNode(@NotNull String name, @Nullable Node previous, @NotNull Code code) {
        super(name, StdKlass.arrayType.type(), previous, code);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitLoadArrayTypeNode(this);
    }

    @Override
    public boolean hasOutput() {
        return true;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("ltarray");
    }

    @Override
    public int getStackChange() {
        return 1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.LT_ARRAY);
    }

    @Override
    public int getLength() {
        return 1;
    }
}
