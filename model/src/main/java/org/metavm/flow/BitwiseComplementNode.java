package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.LongValue;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;

import javax.annotation.Nullable;

public class BitwiseComplementNode extends NodeRT {

    public static BitwiseComplementNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        BitwiseComplementNode node = (BitwiseComplementNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null)
            node = new BitwiseComplementNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope);
        return node;
    }

    public BitwiseComplementNode(Long tmpId,
                                 @NotNull String name,
                                 @Nullable NodeRT previous,
                                 @NotNull ScopeRT scope) {
        super(tmpId, name, null, previous, scope);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitBitwiseComplementNode(this);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return null;
    }

    @Override
    public int execute(MetaFrame frame) {
        var v = (LongValue) frame.pop();
        frame.push(v.bitwiseComplement());
        return MetaFrame.STATE_NEXT;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("bitnot");
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
