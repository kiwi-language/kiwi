package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.flow.rest.Bytecodes;

import javax.annotation.Nullable;

public class PopNode extends Node {

    public PopNode(Long tmpId, @NotNull String name, @Nullable Node previous, @NotNull Code code) {
        super(tmpId, name, null, previous, code);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitPopNode(this);
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("pop");
    }

    @Override
    public int getStackChange() {
        return -1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.POP);
    }

    @Override
    public int getLength() {
        return 1;
    }
}
