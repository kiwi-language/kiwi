package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.SerializeContext;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.flow.rest.TryNodeParam;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

@EntityType("Try节点")
public class TryNode extends ScopeNode {

    public static TryNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
        var node = (TryNode) context.getNode(nodeDTO.getRef());
        if (node == null)
            node = new TryNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), prev, scope);
        return node;
    }

    public TryNode(Long tmpId, String name, @Nullable String code,  NodeRT previous, ScopeRT scope) {
        super(tmpId, name, code, null, previous, scope, false);
    }

    @Override
    protected TryNodeParam getParam(SerializeContext serializeContext) {
        return new TryNodeParam(bodyScope.toDTO(true, serializeContext));
    }

    @NotNull
    @Override
    public TryEndNode getSuccessor() {
        return (TryEndNode) Objects.requireNonNull(super.getSuccessor());
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
        if(getSuccessor() instanceof TryEndNode tryEndNode)
            return List.of(tryEndNode);
        else
            return List.of();
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitTryNode(this);
    }
}
