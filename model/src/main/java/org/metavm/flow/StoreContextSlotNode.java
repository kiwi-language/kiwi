package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.flow.rest.StoreContextSlotNodeParam;
import org.metavm.object.instance.core.Id;

public class StoreContextSlotNode extends NodeRT {

    public static StoreContextSlotNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        StoreContextSlotNodeParam param = nodeDTO.getParam();
        var node = (StoreContextSlotNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            node = new StoreContextSlotNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope,
                    param.contextIndex(), param.slotIndex());
        }
        return node;
    }

    private final int contextIndex;
    private final int slotIndex;

    public StoreContextSlotNode(Long tmpId, @NotNull String name,
                                @Nullable NodeRT previous, @NotNull ScopeRT scope, int contextIndex, int slotIndex) {
        super(tmpId, name, null, previous, scope);
        this.contextIndex = contextIndex;
        this.slotIndex = slotIndex;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitStoreContextSlotNode(this);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return new StoreContextSlotNodeParam(contextIndex, slotIndex);
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
