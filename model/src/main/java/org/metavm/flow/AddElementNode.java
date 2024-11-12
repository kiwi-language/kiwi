package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.NodeDTO;

@EntityType
public class AddElementNode extends NodeRT {

    public static AddElementNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        AddElementNode node = (AddElementNode) context.getNode(nodeDTO.id());
        if (node == null)
            node = new AddElementNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope);
        return node;
    }

    public AddElementNode(Long tmpId, String name,  NodeRT previous, ScopeRT scope) {
        super(tmpId, name, null, previous, scope);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return null;
    }

    @Override
    public int execute(MetaFrame frame) {
        var e = frame.pop();
        var a = frame.pop().resolveArray();
        a.addElement(e);
        return MetaFrame.STATE_NEXT;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("arrayadd");
    }

    @Override
    public int getStackChange() {
        return -2;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitAddElementNode(this);
    }

}
