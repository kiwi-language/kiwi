package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;

@EntityType
public class RaiseNode extends NodeRT {

    public static RaiseNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext entityContext) {
        RaiseNode node = (RaiseNode) entityContext.getNode(Id.parse(nodeDTO.id()));
        if (node == null)
            node = new RaiseNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope);
        return node;
    }

    public RaiseNode(Long tmpId, String name, NodeRT prev, ScopeRT scope) {
        super(tmpId, name, null, prev, scope);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return null;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("raise");
    }

    @Override
    public int getStackChange() {
        return -1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.RAISE);
    }

    @Override
    public int getLength() {
        return 1;
    }

    @Override
    public boolean isExit() {
        return true;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitRaiseNode(this);
    }
}
