package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;

import javax.annotation.Nullable;

public class EqNode extends Node {

    public EqNode(String name,
                  @Nullable Node previous,
                  @NotNull Code code) {
        super(name, Types.getBooleanType(), previous, code);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitEqNode(this);
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("eq");
    }

    @Override
    public int getStackChange() {
        return -1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.EQ);
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
