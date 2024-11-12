package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.Types;
import org.metavm.util.Instances;

@EntityType
public class RemoveElementNode extends NodeRT {

    public static RemoveElementNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        var node = (RemoveElementNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null)
            node = new RemoveElementNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope);
        return node;
    }

    public RemoveElementNode(Long tmpId, String name, NodeRT previous, ScopeRT scope) {
        super(tmpId, name, Types.getBooleanType(), previous, scope);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return null;
    }

    @Override
    public int execute(MetaFrame frame) {
        var arrayInst = frame.pop().resolveArray();
        var elementInst = frame.pop();
        frame.push(Instances.booleanInstance(arrayInst.removeElement(elementInst)));
        return MetaFrame.STATE_NEXT;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("removeElement");
    }

    @Override
    public int getStackChange() {
        return -1;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitDeleteElementNode(this);
    }
}
