package tech.metavm.flow;

import tech.metavm.flow.persistence.NodePO;
import tech.metavm.flow.rest.NodeDTO;

public class ReturnNode extends NodeRT<Void> {

    public ReturnNode(NodeDTO nodeDTO, ScopeRT scope) {
        super(nodeDTO, null, scope);
    }

    public ReturnNode(NodePO nodePO, ScopeRT scope) {
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
        frame.ret();
    }

}
