package org.metavm.flow;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.Type;

@EntityType
@Slf4j
public class ReturnNode extends NodeRT {

    public static ReturnNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext entityContext) {
        ReturnNode node = (ReturnNode) entityContext.getNode(Id.parse(nodeDTO.id()));
        if (node == null)
            node = new ReturnNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope);
        return node;
    }

    public ReturnNode(Long tmpId, String name, NodeRT prev, ScopeRT scope) {
        super(tmpId, name, null, prev, scope);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return null;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("return");
    }

    @Override
    public int getStackChange() {
        return -1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.RETURN);
    }

    @Override
    public int getLength() {
        return 1;
    }

    @Override
    @NotNull
    public Type getType() {
        return getScope().getCallable().getReturnType();
    }

    @Override
    public boolean isExit() {
        return true;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitReturnNode(this);
    }

}
