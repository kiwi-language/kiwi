package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;

import javax.annotation.Nullable;

public class RightShiftNode extends NodeRT {

    public static RightShiftNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        RightShiftNode node = (RightShiftNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null)
            node = new RightShiftNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope);
        return node;
    }

    public RightShiftNode(Long tmpId,
                          @NotNull String name,
                          @Nullable NodeRT previous,
                          @NotNull ScopeRT scope) {
        super(tmpId, name, null, previous, scope);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitRightShiftNode(this);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return null;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("shr");
    }

    @Override
    public int getStackChange() {
        return -1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.RIGHT_SHIFT);
    }

    @Override
    public int getLength() {
        return 1;
    }

    @NotNull
    @Override
    public Type getType() {
        return Types.getLongType();
    }

}
