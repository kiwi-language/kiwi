package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;

import java.util.function.Consumer;

@Entity
public class StoreContextSlotNode extends Node {

    private final int contextIndex;
    private final int slotIndex;

    public StoreContextSlotNode(@NotNull String name,
                                @Nullable Node previous, @NotNull Code code, int contextIndex, int slotIndex) {
        super(name, null, previous, code);
        this.contextIndex = contextIndex;
        this.slotIndex = slotIndex;
    }

    public static Node read(CodeInput input, String name) {
        return new StoreContextSlotNode(name, input.getPrev(), input.getCode(), input.readShort(), input.readShort());
    }

    @Override
    public boolean hasOutput() {
        return false;
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

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitStoreContextSlotNode(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
    }
}
