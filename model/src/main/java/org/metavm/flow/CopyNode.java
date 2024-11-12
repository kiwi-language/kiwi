package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;

@EntityType
public class CopyNode extends NodeRT {

    public static CopyNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        CopyNode node = (CopyNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null)
            node = new CopyNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope);
        return node;
    }

    protected CopyNode(Long tmpId, String name, NodeRT previous, ScopeRT scope) {
        super(tmpId, name, null, previous, scope);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitCopyNode(this);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return null;
    }

    @Override
    public int execute(MetaFrame frame) {
        var sourceInst = frame.pop();
        var copy = sourceInst.resolveDurable().copy();
        frame.push(copy.getReference());
        return MetaFrame.STATE_NEXT;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("copy");
    }

    @Override
    public int getStackChange() {
        return 0;
    }

    @Override
    @NotNull
    public Type getType() {
        return Types.getAnyType();
    }
}
