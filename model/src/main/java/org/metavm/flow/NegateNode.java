package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;

import javax.annotation.Nullable;

public class NegateNode extends Node {

    public NegateNode(String name,
                      @Nullable Node previous,
                      @NotNull Code code
                      ) {
        super(name, null, previous, code);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitNegateNode(this);
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("neg");
    }

    @Override
    public int getStackChange() {
        return 0;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.NEGATE);
    }

    @Override
    public int getLength() {
        return 1;
    }

    @NotNull
    @Override
    public Type getType() {
        return Types.getLongType();
    }

    @Override
    public boolean hasOutput() {
        return true;
    }
}
