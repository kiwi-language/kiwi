package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;

@EntityType
public class NoopNode extends NodeRT {

    public static NoopNode save(NodeDTO nodeDTO, NodeRT prev, Code code, NodeSavingStage stage, IEntityContext context) {
        var node = (NoopNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null)
            node = new NoopNode(nodeDTO.tmpId(), nodeDTO.name(), prev, code);
        return node;
    }

    public NoopNode(Long tmpId, String name, NodeRT previous, Code code) {
        super(tmpId, name, null, previous, code);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return null;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("noop");
    }

    @Override
    public int getStackChange() {
        return 0;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.NOOP);
    }

    @Override
    public int getLength() {
        return 1;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitNoopNode(this);
    }
}
