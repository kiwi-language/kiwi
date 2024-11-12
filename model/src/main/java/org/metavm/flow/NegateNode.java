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

public class NegateNode extends NodeRT {

    public static NegateNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        NegateNode node = (NegateNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null)
            node = new NegateNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope);
        return node;
    }

    public NegateNode(Long tmpId,
                      @NotNull String name,
                      @Nullable NodeRT previous,
                      @NotNull ScopeRT scope
                      ) {
        super(tmpId, name, null, previous, scope);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitNegateNode(this);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return null;
    }

    @Override
    public int execute(MetaFrame frame) {
        var v = (NumberValue) frame.pop();
        frame.push(v.negate());
        return MetaFrame.STATE_NEXT;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("neg");
    }

    @Override
    public int getStackChange() {
        return 0;
    }

    @NotNull
    @Override
    public Type getType() {
        return Types.getLongType();
    }
}
