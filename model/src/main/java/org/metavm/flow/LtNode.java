package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;

import javax.annotation.Nullable;

public class LtNode extends Node {

    public LtNode(Long tmpId,
                  @NotNull String name,
                  @Nullable Node previous,
                  @NotNull Code code) {
        super(tmpId, name, Types.getBooleanType(), previous, code);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitLtNode(this);
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("lt");
    }

    @Override
    public int getStackChange() {
        return -1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.LT);
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
