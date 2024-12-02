package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;

import javax.annotation.Nullable;

public class DoubleRemNode extends Node {

    public DoubleRemNode(String name,
                         @Nullable Node previous,
                         @NotNull Code code) {
        super(name, null, previous, code);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitDoubleRemNode(this);
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("drem");
    }

    @Override
    public int getStackChange() {
        return -1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.DOUBLE_REM);
    }

    @Override
    public int getLength() {
        return 1;
    }

    @NotNull
    @Override
    public Type getType() {
        return Types.getDoubleType();
    }

    @Override
    public boolean hasOutput() {
        return true;
    }
}
