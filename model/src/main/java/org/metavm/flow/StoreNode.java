package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;

import javax.annotation.Nullable;

public class StoreNode extends VariableAccessNode {

    public StoreNode(@NotNull String name, @Nullable Node previous, @NotNull Code code, int index) {
        super(name, null, previous, code, index);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitStoreNode(this);
    }

    @Override
    public boolean hasOutput() {
        return false;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("store " + index);
    }

    @Override
    public int getStackChange() {
        return -1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.STORE);
        output.writeShort(index);
    }

    @Override
    public int getLength() {
        return 3;
    }

    public int getIndex() {
        return index;
    }
}
