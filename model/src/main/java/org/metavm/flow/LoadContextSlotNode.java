package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.LoadContextSlotNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeParser;

import java.util.Objects;

public class LoadContextSlotNode extends NodeRT {

    public static LoadContextSlotNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        LoadContextSlotNodeParam param = nodeDTO.getParam();
        var node = (LoadContextSlotNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            var type = TypeParser.parseType(nodeDTO.outputType(), context);
            node = new LoadContextSlotNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), type, prev, scope,
                    param.contextIndex(), param.slotIndex());
        }
        return node;
    }

    private final int contextIndex;
    private final int slotIndex;

    public LoadContextSlotNode(Long tmpId, @NotNull String name, @Nullable String code, Type outputType,
                               @Nullable NodeRT previous, @NotNull ScopeRT scope, int contextIndex, int slotIndex) {
        super(tmpId, name, code, outputType, previous, scope);
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
    public NodeExecResult execute(MetaFrame frame) {
        return next(frame.loadContextSlot(contextIndex, slotIndex));
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("loadContextSlot " + contextIndex + " " + slotIndex);
    }

}
