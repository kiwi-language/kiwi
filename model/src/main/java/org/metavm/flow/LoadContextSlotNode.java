package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.metavm.entity.ElementVisitor;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.object.type.Type;

import java.util.Objects;

public class LoadContextSlotNode extends Node {

    private final int contextIndex;
    private final int slotIndex;

    public LoadContextSlotNode(Long tmpId, @NotNull String name, Type outputType,
                               @Nullable Node previous, @NotNull Code code, int contextIndex, int slotIndex) {
        super(tmpId, name, outputType, previous, code);
        this.contextIndex = contextIndex;
        this.slotIndex = slotIndex;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitLoadContextSlotNode(this);
    }

    @Override
    @NotNull
    public Type getType() {
        return Objects.requireNonNull(super.getType());
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("loadContextSlot " + contextIndex + " " + slotIndex);
    }

    @Override
    public int getStackChange() {
        return 1;
    }

    @Override
    public void writeCode(CodeOutput output) {
       output.write(Bytecodes.LOAD_CONTEXT_SLOT);
       output.writeShort(contextIndex);
       output.writeShort(slotIndex);
    }

    @Override
    public int getLength() {
        return 5;
    }

}
