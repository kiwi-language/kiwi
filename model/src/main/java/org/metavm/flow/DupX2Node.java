package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.GotoNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;

import javax.annotation.Nullable;

public class DupX2Node extends NodeRT {

    public static DupX2Node save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        var node = (DupX2Node) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null)
            node = new DupX2Node(nodeDTO.tmpId(), nodeDTO.name(), prev, scope);
        return node;
    }

    public DupX2Node(Long tmpId, @NotNull String name, @Nullable NodeRT previous, @NotNull ScopeRT scope) {
        super(tmpId, name, null, previous, scope);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitDupX2Node(this);
    }

    @Override
    protected GotoNodeParam getParam(SerializeContext serializeContext) {
        return null;
    }

    @Override
    public int execute(MetaFrame frame) {
        frame.dupX2();
        return MetaFrame.STATE_NEXT;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("dup_x2");
    }

    @Override
    public int getStackChange() {
        return 1;
    }

    @NotNull
    @Override
    public Type getType() {
        return Types.getAnyType();
    }
}
