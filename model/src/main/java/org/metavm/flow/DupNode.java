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

public class DupNode extends NodeRT {

    public static DupNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        var node = (DupNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null)
            node = new DupNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope);
        return node;
    }

    public DupNode(Long tmpId, @NotNull String name, @Nullable NodeRT previous, @NotNull ScopeRT scope) {
        super(tmpId, name, null, previous, scope);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitDupNode(this);
    }

    @Override
    protected GotoNodeParam getParam(SerializeContext serializeContext) {
        return null;
    }

    @Override
    public int execute(MetaFrame frame) {
        frame.dup();
        return MetaFrame.STATE_NEXT;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("dup");
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
