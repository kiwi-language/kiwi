package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.expression.FlowParsingContext;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.flow.rest.StoreContextSlotNodeParam;
import org.metavm.object.instance.core.Id;

public class StoreContextSlotNode extends NodeRT {

    public static StoreContextSlotNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        StoreContextSlotNodeParam param = nodeDTO.getParam();
        var node = (StoreContextSlotNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            var parsingContext = FlowParsingContext.create(scope, prev, context);
            var value = ValueFactory.create(param.value(), parsingContext);
            node = new StoreContextSlotNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), prev, scope,
                    param.contextIndex(), param.slotIndex(), value);
        }
        return node;
    }

    private final int contextIndex;
    private final int slotIndex;
    private final Value value;

    public StoreContextSlotNode(Long tmpId, @NotNull String name, @Nullable String code,
                                @Nullable NodeRT previous, @NotNull ScopeRT scope, int contextIndex, int slotIndex, Value value) {
        super(tmpId, name, code, null, previous, scope);
        this.contextIndex = contextIndex;
        this.slotIndex = slotIndex;
        this.value = value;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitStoreContextSlotNode(this);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return new StoreContextSlotNodeParam(contextIndex, slotIndex, value.toDTO());
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        frame.storeContextSlot(contextIndex, slotIndex, value.evaluate(frame));
        return next();
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("storeContextSlot " + contextIndex + " " + slotIndex + " " + value.getText());
    }

}
