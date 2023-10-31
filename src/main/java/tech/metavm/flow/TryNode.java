package tech.metavm.flow;

import tech.metavm.entity.IEntityContext;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.flow.rest.TryNodeParamDTO;
import tech.metavm.util.NncUtils;

import java.util.List;

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

    public TryEndNode getTryEndNode() {
        return (TryEndNode) NncUtils.requireNonNull(getSuccessor());
    }

    @Override
    public void execute(MetaFrame frame) {
        frame.enterTrySection(this);
        if(!bodyScope.isEmpty()) {
            frame.jumpTo(bodyScope.getFirstNode());
        }
    }

    @Override
    protected List<Object> nodeBeforeRemove() {
        if(getSuccessor() instanceof TryEndNode tryEndNode) {
            return List.of(tryEndNode);
        }
        else {
            return List.of();
        }
    }
}
