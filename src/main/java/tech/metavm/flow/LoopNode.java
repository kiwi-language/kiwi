package tech.metavm.flow;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityContext;
import tech.metavm.entity.EntityType;
import tech.metavm.flow.persistence.NodePO;
import tech.metavm.flow.rest.LoopParam;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.util.NncUtils;

@EntityType("循环节点")
public class LoopNode extends NodeRT<LoopParam> {

    private Value condition;
    private final NodeRT<?> firstChild;

    public LoopNode(NodeDTO nodeDTO, ValueDTO valueDTO, NodeRT<?> firstChild, ScopeRT scope) {
        super(nodeDTO, null, scope);
        this.firstChild = firstChild;
        this.condition = ValueFactory.getValue(valueDTO, getParsingContext());
    }

//    public LoopNode(NodePO nodePO, LoopParam param, EntityContext context) {
//        super(nodePO, context);
//        firstChild = NncUtils.get(param.firstChildId(), context::getNode);
//        setParam(param);
//    }

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
