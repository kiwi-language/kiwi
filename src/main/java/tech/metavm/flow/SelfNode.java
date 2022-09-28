package tech.metavm.flow;

import tech.metavm.flow.persistence.NodePO;
import tech.metavm.flow.rest.NodeDTO;

public class SelfNode extends NodeRT<Void> {

    public SelfNode(NodeDTO nodeDTO, ScopeRT scope) {
        super(nodeDTO, scope.getFlow().getType(), scope);
    }

    public SelfNode(NodePO nodePO, ScopeRT scope) {
        super(nodePO, scope);
    }

    @Override
    protected void setParam(Void param) {

    }

    @Override
    protected Void getParam(boolean forPersistence) {
        return null;
    }

    @Override
    public void execute(FlowFrame frame) {
        frame.setResult(frame.getSelf());
    }
}
