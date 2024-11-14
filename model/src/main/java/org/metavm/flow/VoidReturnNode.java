package org.metavm.flow;

import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;

public class VoidReturnNode extends NodeRT {

    public static VoidReturnNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext entityContext) {
        var node = (VoidReturnNode) entityContext.getNode(Id.parse(nodeDTO.id()));
        if (node == null)
            node = new VoidReturnNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope);
        return node;
    }

    public VoidReturnNode(Long tmpId, String name, NodeRT prev, ScopeRT scope) {
        super(tmpId, name, null, prev, scope);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return null;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("void-ret");
    }

    @Override
    public int getStackChange() {
        return 0;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.VOID_RETURN);
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
        return visitor.visitReturn(this);
    }

}
