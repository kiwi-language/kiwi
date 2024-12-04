package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;

import javax.annotation.Nullable;

public class FloatToLongNode extends Node {

    public FloatToLongNode(@NotNull String name, @Nullable Node previous, @NotNull Code code) {
        super(name, null, previous, code);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitFloatToLongNode(this);
    }

    @Override
    public boolean hasOutput() {
        return true;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("f2l");
    }

    @Override
    public int getStackChange() {
        return 0;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.FLOAT_TO_LONG);
    }

    @Override
    public int getLength() {
        return 1;
    }

    @Override
    @NotNull
    public Type getType() {
        return Types.getLongType();
    }
}
