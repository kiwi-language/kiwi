package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;

import javax.annotation.Nullable;

@EntityType
public class GetElementNode extends NodeRT {

    public static GetElementNode save(NodeDTO nodeDTO, @Nullable NodeRT prev, Code code, NodeSavingStage stage, IEntityContext context) {
        GetElementNode node = (GetElementNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null)
            node = new GetElementNode(nodeDTO.tmpId(), nodeDTO.name(), prev, code);
        return node;
    }

    public GetElementNode(Long tmpId, String name, NodeRT previous, Code code) {
        super(tmpId, name, null, previous, code);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return null;
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
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.GET_ELEMENT);
    }

    @Override
    public int getLength() {
        return 1;
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
