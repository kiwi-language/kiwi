package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.flow.rest.TryExitNodeParam;
import org.metavm.object.instance.core.Id;

@EntityType
public class TryExitNode extends NodeRT {

    public static TryExitNode save(NodeDTO nodeDTO, NodeRT prev, Code code, NodeSavingStage stage, IEntityContext context) {
        var node = (TryExitNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            var param = (TryExitNodeParam) nodeDTO.param();
            node = new TryExitNode(nodeDTO.tmpId(), nodeDTO.name(), prev, code, param.variableIndex());
        }
        return node;
    }

    private final int variableIndex;

    public TryExitNode(Long tmpId, String name, NodeRT previous, Code code, int variableIndex) {
        super(tmpId, name, null, previous, code);
        this.variableIndex = variableIndex;
    }

    @Override
    protected TryExitNodeParam getParam(SerializeContext serializeContext) {
        return new TryExitNodeParam(variableIndex);
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("try-exit variable = " + variableIndex);
    }

    @Override
    public int getStackChange() {
        return 0;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.TRY_EXIT);
        output.writeShort(variableIndex);
    }

    @Override
    public int getLength() {
        return 3;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitTryExitNode(this);
    }

    public int getVariableIndex() {
        return variableIndex;
    }
}
