package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;

import javax.annotation.Nullable;

public class UnsignedRightShiftNode extends Node {

    public UnsignedRightShiftNode(Long tmpId,
                                  @NotNull String name,
                                  @Nullable Node previous,
                                  @NotNull Code code) {
        super(tmpId, name, null, previous, code);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitUnsignedRightShift(this);
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("ushr");
    }

    @Override
    public int getStackChange() {
        return -1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.UNSIGNED_RIGHT_SHIFT);
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

}
