package tech.metavm.flow;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.flow.rest.NodeDTO;

@EntityType("当前记录节点")
public class SelfNode extends NodeRT<Void> {

    public static SelfNode create(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope, IEntityContext context) {
            return new SelfNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope);
    }

    public SelfNode(Long tmpId, String name, NodeRT<?> prev, ScopeRT scope) {
        super(tmpId, name, scope.getFlow().getDeclaringType(), prev, scope);
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
