package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.BooleanValue;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;

import javax.annotation.Nullable;

public class AndNode extends NodeRT {

    public static AndNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        AndNode node = (AndNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null)
            node = new AndNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope);
        return node;
    }

    public AndNode(Long tmpId,
                   @NotNull String name,
                   @Nullable NodeRT previous,
                   @NotNull ScopeRT scope) {
        super(tmpId, name, null, previous, scope);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitAndNode(this);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return null;
    }

    @Override
    public int execute(MetaFrame frame) {
        var v2 = (BooleanValue) frame.pop();
        var v1 = (BooleanValue) frame.pop();
        frame.push(v1.and(v2));
        return MetaFrame.STATE_NEXT;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("and");
    }

    @Override
    public int getStackChange() {
        return -1;
    }

    @NotNull
    @Override
    public Type getType() {
        return Types.getBooleanType();
    }
}
