package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;

import javax.annotation.Nullable;

public class PopNode extends NodeRT {

    public static PopNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        var node = (PopNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null)
            node = new PopNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope);
        return node;
    }

    public PopNode(Long tmpId, @NotNull String name, @Nullable NodeRT previous, @NotNull ScopeRT scope) {
        super(tmpId, name, null, previous, scope);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitPopNode(this);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return null;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("pop");
    }

    @Override
    public int getStackChange() {
        return -1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.POP);
    }

    @Override
    public int getLength() {
        return 1;
    }
}
