package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;

import javax.annotation.Nullable;

public class GtNode extends Node {

    public GtNode(String name,
                  @Nullable Node previous,
                  @NotNull Code code) {
        super(name, Types.getBooleanType(), previous, code);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitGtNode(this);
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("gt");
    }

    @Override
    public int getStackChange() {
        return -1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.GT);
    }

    @Override
    public int getLength() {
        return 1;
    }

    @NotNull
    @Override
    public Type getType() {
        return Types.getBooleanType();
    }

    @Override
    public boolean hasOutput() {
        return true;
    }

}
