package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.metavm.entity.ElementVisitor;
import org.metavm.flow.rest.Bytecodes;

public class StoreContextSlotNode extends Node {

    private final int contextIndex;
    private final int slotIndex;

    public StoreContextSlotNode(Long tmpId, @NotNull String name,
                                @Nullable Node previous, @NotNull Code code, int contextIndex, int slotIndex) {
        super(tmpId, name, null, previous, code);
        this.contextIndex = contextIndex;
        this.slotIndex = slotIndex;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitStoreContextSlotNode(this);
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("storeContextSlot " + contextIndex + " " + slotIndex);
    }

    @Override
    public int getStackChange() {
        return -1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.STORE_CONTEXT_SLOT);
        output.writeShort(contextIndex);
        output.writeShort(slotIndex);
    }

    @Override
    public int getLength() {
        return 5;
    }

}
