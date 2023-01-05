package tech.metavm.flow;

import tech.metavm.entity.*;
import tech.metavm.flow.persistence.NodePO;
import tech.metavm.flow.rest.LoopParam;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.object.instance.query.FlowParsingContext;
import tech.metavm.object.instance.query.ParsingContext;
import tech.metavm.util.NncUtils;

@EntityType("循环节点")
public class LoopNode extends NodeRT<LoopParam> {

    public static LoopNode create(NodeDTO nodeDTO, IEntityContext context) {
        LoopParam loopParam = nodeDTO.getParam();
        NodeRT<?> firstChild = context.getNode(loopParam.firstChildId());
        NodeRT<?> prev = NncUtils.get(nodeDTO.prevId(), context::getNode);
        ScopeRT scope = context.getScope(nodeDTO.scopeId());
        ParsingContext parsingContext = FlowParsingContext.create(scope, prev, context);
        Value condition = ValueFactory.getValue(loopParam.condition(), parsingContext);
        return new LoopNode(nodeDTO, condition, firstChild, context.getScope(nodeDTO.scopeId()));
    }

    @EntityField("循环条件")
    private Value condition;
    @EntityField("首节点")
    private final NodeRT<?> firstChild;

    public LoopNode(NodeDTO nodeDTO, Value condition, NodeRT<?> firstChild, ScopeRT scope) {
        super(nodeDTO, null, scope);
        this.firstChild = firstChild;
        this.condition = condition;
    }

    @Override
    protected void setParam(LoopParam param, IEntityContext entityContext) {
        condition = ValueFactory.getValue(param.condition(), getParsingContext(entityContext));
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
