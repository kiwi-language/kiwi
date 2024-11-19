package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;

import javax.annotation.Nullable;

public class DupX2Node extends Node {

    public DupX2Node(@NotNull String name, @Nullable Node previous, @NotNull Code code) {
        super(name, null, previous, code);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitDupX2Node(this);
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("dup_x2");
    }

    @Override
    public int getStackChange() {
        return 1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.DUP_X2);
    }

    @Override
    public int getLength() {
        return 1;
    }

    @NotNull
    @Override
    public Type getType() {
        return Types.getAnyType();
    }
}
