package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.flow.rest.SetElementNodeParam;
import org.metavm.object.instance.core.LongValue;

@EntityType
public class SetElementNode extends NodeRT {

    public static SetElementNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        SetElementNode node = (SetElementNode) context.getNode(nodeDTO.id());
        if (node == null)
            node = new SetElementNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope);
        return node;
    }

    public SetElementNode(Long tmpId, String name, NodeRT previous, ScopeRT scope) {
        super(tmpId, name, null, previous, scope);
    }

    @Override
    protected SetElementNodeParam getParam(SerializeContext serializeContext) {
        return null;
    }

    @Override
    public int execute(MetaFrame frame) {
        var e = frame.pop();
        var i = ((LongValue) frame.pop()).getValue().intValue();
        var a = frame.pop().resolveArray();
        a.setElement(i, e);
        return MetaFrame.STATE_NEXT;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("astore");
    }

    @Override
    public int getStackChange() {
        return -3;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitSetElementNode(this);
    }

}
