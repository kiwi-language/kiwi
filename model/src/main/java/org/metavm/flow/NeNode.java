package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;
import org.metavm.util.Instances;

import javax.annotation.Nullable;

public class NeNode extends NodeRT {

    public static NeNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        NeNode node = (NeNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null)
            node = new NeNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope);
        return node;
    }

    public NeNode(Long tmpId,
                  @NotNull String name,
                  @Nullable NodeRT previous,
                  @NotNull ScopeRT scope) {
        super(tmpId, name, Types.getBooleanType(), previous, scope);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitNeNode(this);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return null;
    }

    @Override
    public int execute(MetaFrame frame) {
        var v2 = frame.pop();
        var v1 = frame.pop();
        frame.push(Instances.notEquals(v1, v2));
        return MetaFrame.STATE_NEXT;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("ne");
    }

    @Override
    public int getStackChange() {
        return 0;
    }

    @NotNull
    @Override
    public Type getType() {
        return Types.getBooleanType();
    }

}
