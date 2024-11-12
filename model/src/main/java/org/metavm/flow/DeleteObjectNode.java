package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Reference;

@EntityType
public class DeleteObjectNode extends NodeRT {

    public static DeleteObjectNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        DeleteObjectNode node = (DeleteObjectNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null)
            node = new DeleteObjectNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope);
        return node;
    }

    public DeleteObjectNode(Long tmpId, String name,  NodeRT prev, ScopeRT scope) {
        super(tmpId, name, null, prev, scope);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return null;
    }

    @Override
    public int execute(MetaFrame frame) {
        frame.deleteInstance((Reference) frame.pop());
        return MetaFrame.STATE_NEXT;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("delete");
    }

    @Override
    public int getStackChange() {
        return -1;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitDeleteObjectNode(this);
    }
}
