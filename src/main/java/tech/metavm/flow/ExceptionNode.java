package tech.metavm.flow;

import tech.metavm.flow.persistence.NodePO;
import tech.metavm.flow.rest.NodeDTO;

public class ExceptionNode extends NodeRT<Void> {

    public ExceptionNode(NodeDTO nodeDTO, ScopeRT scope) {
        super(nodeDTO, null, scope);
    }

    public ExceptionNode(NodePO nodePO, ScopeRT scope) {
        super(nodePO, scope);
    }

    @Override
    protected void setParam(Void param) {}

    @Override
    protected Void getParam(boolean persisting) {
        return null;
    }

    @Override
    public void execute(FlowFrame frame) {
    }

}
