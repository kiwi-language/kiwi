package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;

import javax.annotation.Nullable;

public class NeNode extends Node {

    public NeNode(String name,
                  @Nullable Node previous,
                  @NotNull Code code) {
        super(name, Types.getBooleanType(), previous, code);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitNeNode(this);
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("ne");
    }

    @Override
    public int getStackChange() {
        return 0;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.NE);
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

}
