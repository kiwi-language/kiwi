package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.type.Types;

import javax.annotation.Nullable;

public class LoadParentNode extends Node {

    public final int index;

    public LoadParentNode(@NotNull String name, @Nullable Node previous, @NotNull Code code, int index) {
        super(name, Types.getAnyType(), previous, code);
        this.index = index;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitLoadParentNode(this);
    }

    @Override
    public boolean hasOutput() {
        return true;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("loadParent " + index);
    }

    @Override
    public int getStackChange() {
        return 0;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.LOAD_PARENT);
        output.writeShort(index);
    }

    @Override
    public int getLength() {
        return 3;
    }
}
