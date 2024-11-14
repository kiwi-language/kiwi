package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.common.ErrorCode;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.util.BusinessException;

@EntityType
public class ClearArrayNode extends NodeRT {

    public static ClearArrayNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        var node = (ClearArrayNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null)
            node = new ClearArrayNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope);
        return node;
    }

    public ClearArrayNode(Long tmpId, String name,
                          NodeRT previous, ScopeRT scope) {
        super(tmpId, name, null, previous, scope);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitClearArrayNode(this);
    }

    private Value check(@NotNull Value array) {
        if(!array.getType().isArray())
            throw new BusinessException(ErrorCode.NOT_AN_ARRAY_VALUE);
        return array;
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return null;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("arrayclear");
    }

    @Override
    public int getStackChange() {
        return -1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.CLEAR_ARRAY);
    }

    @Override
    public int getLength() {
        return 1;
    }

}
