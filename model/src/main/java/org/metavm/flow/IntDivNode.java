package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;

import javax.annotation.Nullable;

public class IntDivNode extends Node {

    public IntDivNode(String name,
                      @Nullable Node previous,
                      @NotNull Code code) {
        super(name, null, previous, code);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitIntDivNode(this);
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("idiv");
    }

    @Override
    public int getStackChange() {
        return -1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.INT_MUL);
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
