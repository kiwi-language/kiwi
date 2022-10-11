package tech.metavm.flow;

import tech.metavm.entity.Entity;
import tech.metavm.flow.persistence.NodePO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.flow.rest.LoopParam;
import tech.metavm.util.NncUtils;

public class LoopNode extends NodeRT<LoopParam> {

    private Value condition;
    private final NodeRT<?> firstChild;

    public LoopNode(NodeDTO nodeDTO, LoopParam param, ScopeRT scope) {
        super(nodeDTO, null, scope);
        firstChild = NncUtils.get(param.firstChildId(), this::getNodeFromContext);
        setParam(param);
    }

    public LoopNode(NodePO nodePO, LoopParam param, ScopeRT scope) {
        super(nodePO, scope);
        firstChild = NncUtils.get(param.firstChildId(), this::getNodeFromContext);
        setParam(param);
    }

    @Override
    protected void setParam(LoopParam param) {
        condition = ValueFactory.getValue(param.condition(), getParsingContext());
    }

    public NodeRT<?> getFirstChild() {
        return firstChild;
    }

    public Value getCondition() {
        return condition;
    }

    @Override
    protected LoopParam getParam(boolean persisting) {
        return new LoopParam(
                condition.toDTO(persisting),
                NncUtils.get(firstChild, Entity::getId)
        );
    }

    @Override
    public void execute(FlowFrame frame) {
    }

}
