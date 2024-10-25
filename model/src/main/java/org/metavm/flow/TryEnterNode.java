package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.api.EntityType;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.flow.rest.TryEnterNodeParam;
import org.metavm.object.instance.core.Id;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

@EntityType
public class TryEnterNode extends ScopeNode {

    public static TryEnterNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        var node = (TryEnterNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null)
            node = new TryEnterNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), prev, scope);
        return node;
    }

    public TryEnterNode(Long tmpId, String name, @Nullable String code, NodeRT previous, ScopeRT scope) {
        super(tmpId, name, code, null, previous, scope, false);
    }

    @Override
    protected TryEnterNodeParam getParam(SerializeContext serializeContext) {
        return new TryEnterNodeParam(bodyScope.toDTO(true, serializeContext));
    }

    @NotNull
    @Override
    public TryExitNode getSuccessor() {
        return (TryExitNode) Objects.requireNonNull(super.getSuccessor());
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        frame.enterTrySection(this);
        if(bodyScope.isNotEmpty())
            return NodeExecResult.jump(bodyScope.tryGetFirstNode());
        else
            return next();
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("try ");
        bodyScope.writeCode(writer);
    }


    @Override
    protected List<Object> nodeBeforeRemove() {
        if(super.getSuccessor() instanceof TryExitNode tryExitNode)
            return List.of(tryExitNode);
        else
            return List.of();
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitTryNode(this);
    }
}
