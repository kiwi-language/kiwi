package tech.metavm.flow;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.flow.rest.NodeDTO;

@EntityType("当前记录节点")
public class SelfNode extends NodeRT<Void> {

    public SelfNode(NodeDTO nodeDTO, ScopeRT scope) {
        super(nodeDTO, scope.getFlow().getType(), scope);
    }

    @Override
    protected void setParam(Void param, IEntityContext entityContext) {

    }

    @Override
    protected Void getParam(boolean persisting) {
        return null;
    }

    @Override
    public void execute(FlowFrame frame) {
        frame.setResult(frame.getSelf());
    }
}
