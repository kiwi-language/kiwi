package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.flow.rest.TryNodeParamDTO;
import tech.metavm.util.NncUtils;

import java.util.List;
import java.util.Objects;

@EntityType("尝试节点")
public class TryNode extends ScopeNode<TryNodeParamDTO> {

    public static TryNode create(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope, IEntityContext context) {
        return new TryNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope);
    }

    public TryNode(Long tmpId, String name, NodeRT<?> previous, ScopeRT scope) {
        super(tmpId, name, null, previous, scope, false);
    }

    @Override
    protected TryNodeParamDTO getParam(boolean persisting) {
        return new TryNodeParamDTO(bodyScope.toDTO(true));
    }

    @Override
    protected void setParam(TryNodeParamDTO param, IEntityContext context) {

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
