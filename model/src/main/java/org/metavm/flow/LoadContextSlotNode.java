package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.flow.rest.LoadContextSlotNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeParser;

import java.util.Objects;

public class LoadContextSlotNode extends NodeRT {

    public static LoadContextSlotNode save(NodeDTO nodeDTO, NodeRT prev, Code code, NodeSavingStage stage, IEntityContext context) {
        LoadContextSlotNodeParam param = nodeDTO.getParam();
        var node = (LoadContextSlotNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            var type = TypeParser.parseType(nodeDTO.outputType(), context);
            node = new LoadContextSlotNode(nodeDTO.tmpId(), nodeDTO.name(), type, prev, code,
                    param.contextIndex(), param.slotIndex());
        }
        return node;
    }

    private final int contextIndex;
    private final int slotIndex;

    public LoadContextSlotNode(Long tmpId, @NotNull String name, Type outputType,
                               @Nullable NodeRT previous, @NotNull Code code, int contextIndex, int slotIndex) {
        super(tmpId, name, outputType, previous, code);
        this.contextIndex = contextIndex;
        this.slotIndex = slotIndex;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitLoadContextSlotNode(this);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return new LoadContextSlotNodeParam(contextIndex, slotIndex, getType().toExpression(serializeContext));
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
