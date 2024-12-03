package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;

import javax.annotation.Nullable;

public class IntAddNode extends Node {

    public IntAddNode(String name,
                      @Nullable Node previous,
                      @NotNull Code code) {
        super(name, null, previous, code);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitIntAddNode(this);
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("iadd");
    }

    @Override
    public int getStackChange() {
        return -1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.INT_ADD);
    }

    @Override
    public int getLength() {
        return 1;
    }

    @NotNull
    @Override
    public Type getType() {
        return Types.getIntType();
    }

    @Override
    public boolean hasOutput() {
        return true;
    }
}
