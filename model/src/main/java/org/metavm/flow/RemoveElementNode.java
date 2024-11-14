package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.Types;

@EntityType
public class RemoveElementNode extends NodeRT {

    public static RemoveElementNode save(NodeDTO nodeDTO, NodeRT prev, Code code, NodeSavingStage stage, IEntityContext context) {
        var node = (RemoveElementNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null)
            node = new RemoveElementNode(nodeDTO.tmpId(), nodeDTO.name(), prev, code);
        return node;
    }

    public RemoveElementNode(Long tmpId, String name, NodeRT previous, Code code) {
        super(tmpId, name, Types.getBooleanType(), previous, code);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return null;
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
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.DELETE_ELEMENT);
    }

    @Override
    public int getLength() {
        return 1;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitDeleteElementNode(this);
    }
}
