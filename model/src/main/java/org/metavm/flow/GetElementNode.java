package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.LongValue;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;

import javax.annotation.Nullable;

@EntityType
public class GetElementNode extends NodeRT {

    public static GetElementNode save(NodeDTO nodeDTO, @Nullable NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        GetElementNode node = (GetElementNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null)
            node = new GetElementNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope);
        return node;
    }

    public GetElementNode(Long tmpId, String name, NodeRT previous, ScopeRT scope) {
        super(tmpId, name, null, previous, scope);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return null;
    }

    @Override
    public int execute(MetaFrame frame) {
        var indexInst = (LongValue) frame.pop();
        var arrayInst = frame.pop().resolveArray();
        frame.push(arrayInst.get(indexInst.getValue().intValue()));
        return MetaFrame.STATE_NEXT;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("array-load");
    }

    @Override
    public int getStackChange() {
        return -1;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitGetElementNode(this);
    }

    @NotNull
    @Override
    public Type getType() {
        return Types.getAnyType();
    }
}
