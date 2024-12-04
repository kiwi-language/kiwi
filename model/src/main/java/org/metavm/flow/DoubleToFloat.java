package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;

import javax.annotation.Nullable;

public class DoubleToFloat extends Node {

    public DoubleToFloat(@NotNull String name, @Nullable Node previous, @NotNull Code code) {
        super(name, null, previous, code);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitDoubleToFloatNode(this);
    }

    @Override
    public boolean hasOutput() {
        return true;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("d2f");
    }

    @Override
    public int getStackChange() {
        return 0;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.DOUBLE_TO_FLOAT);
    }

    @Override
    public int getLength() {
        return 1;
    }

    @Override
    @NotNull
    public Type getType() {
        return Types.getFloatType();
    }
}
