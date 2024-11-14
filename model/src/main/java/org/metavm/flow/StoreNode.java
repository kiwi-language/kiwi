package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.flow.rest.StoreNodeParam;
import org.metavm.object.instance.core.Id;

import javax.annotation.Nullable;

public class StoreNode extends VariableAccessNode {

    public static StoreNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        StoreNodeParam param = nodeDTO.getParam();
        var node = (StoreNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null)
            node = new StoreNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope, param.index());
        return node;
    }

    public StoreNode(Long tmpId, @NotNull String name, @Nullable NodeRT previous, @NotNull ScopeRT scope, int index) {
        super(tmpId, name, null, previous, scope, index);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitStoreNode(this);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return new StoreNodeParam(index);
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("store " + index);
    }

    @Override
    public int getStackChange() {
        return -1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.STORE);
        output.writeShort(index);
    }

    @Override
    public int getLength() {
        return 3;
    }

    public int getIndex() {
        return index;
    }
}
