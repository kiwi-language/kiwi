package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;

import javax.annotation.Nullable;

public class IntNegNode extends Node {

    public IntNegNode(String name,
                      @Nullable Node previous,
                      @NotNull Code code
                      ) {
        super(name, null, previous, code);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitIntNegNode(this);
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("ineg");
    }

    @Override
    public int getStackChange() {
        return 0;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.INT_NEG);
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
