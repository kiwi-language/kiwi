package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.NumberValue;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;

import javax.annotation.Nullable;

public class GeNode extends NodeRT {

    public static GeNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        GeNode node = (GeNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null)
            node = new GeNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope);
        return node;
    }

    public GeNode(Long tmpId,
                  @NotNull String name,
                  @Nullable NodeRT previous,
                  @NotNull ScopeRT scope) {
        super(tmpId, name, Types.getBooleanType(), previous, scope);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitGeNode(this);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return null;
    }

    @Override
    public int execute(MetaFrame frame) {
        var v2 = (NumberValue) frame.pop();
        var v1 = (NumberValue) frame.pop();
        frame.push(v1.ge(v2));
        return MetaFrame.STATE_NEXT;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("ge");
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
