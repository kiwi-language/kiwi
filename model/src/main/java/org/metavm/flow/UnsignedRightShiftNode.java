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

public class UnsignedRightShiftNode extends NodeRT {

    public static UnsignedRightShiftNode save(NodeDTO nodeDTO, NodeRT prev, Code code, NodeSavingStage stage, IEntityContext context) {
        UnsignedRightShiftNode node = (UnsignedRightShiftNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null)
            node = new UnsignedRightShiftNode(nodeDTO.tmpId(), nodeDTO.name(), prev, code);
        return node;
    }

    public UnsignedRightShiftNode(Long tmpId,
                                  @NotNull String name,
                                  @Nullable NodeRT previous,
                                  @NotNull Code code) {
        super(tmpId, name, null, previous, code);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitUnsignedRightShift(this);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return null;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("ushr");
    }

    @Override
    public int getStackChange() {
        return -1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.UNSIGNED_RIGHT_SHIFT);
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
