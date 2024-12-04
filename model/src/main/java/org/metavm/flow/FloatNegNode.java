package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;

import javax.annotation.Nullable;

public class FloatNegNode extends Node {

    public FloatNegNode(String name,
                        @Nullable Node previous,
                        @NotNull Code code
                      ) {
        super(name, null, previous, code);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitFloatNegNode(this);
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("fneg");
    }

    @Override
    public int getStackChange() {
        return 0;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.FLOAT_NEG);
    }

    @Override
    public int getLength() {
        return 1;
    }

    @NotNull
    @Override
    public Type getType() {
        return Types.getFloatType();
    }

    @Override
    public boolean hasOutput() {
        return true;
    }
}
