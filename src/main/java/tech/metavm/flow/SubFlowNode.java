package tech.metavm.flow;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.SerializeContext;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.expression.ParsingContext;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.flow.rest.SubFlowParam;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.util.NncUtils;

import java.util.List;

@EntityType("子流程节点")
public class SubFlowNode extends CallNode<SubFlowParam> {

    public static SubFlowNode create(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope, IEntityContext context) {
        SubFlowParam param = nodeDTO.getParam();
        FlowParsingContext parsingContext = FlowParsingContext.create(scope, prev, context);
        Value self = ValueFactory.create(param.getSelf(), parsingContext);
        var subFlow = context.getFlow(param.getFlowRef());
        List<Argument> arguments = NncUtils.biMap(
                subFlow.getParameters(),
                param.getArguments(),
                (p, a) -> new Argument(a.tmpId(), p, ValueFactory.create(a.value(), parsingContext))
        );
        return new SubFlowNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope, self, arguments, subFlow);
    }

    @ChildEntity("目标对象")
    private Value selfId;

    public SubFlowNode(Long tmpId, String name, NodeRT<?> prev, ScopeRT scope, Value selfId, List<Argument> arguments,
                       Flow subFlow) {
        super(tmpId, name,  prev, scope, arguments, subFlow);
        this.selfId = selfId;
    }

    @Override
    protected void setParam(SubFlowParam param, IEntityContext entityContext) {
        ParsingContext parsingContext = getParsingContext(entityContext);
        selfId = ValueFactory.create(param.getSelf(), parsingContext);
        super.setCallParam(param, entityContext);
    }

    @Override
    protected SubFlowParam getParam(boolean persisting) {
        try(var context = SerializeContext.enter()) {
            return new SubFlowParam(
                    selfId.toDTO(persisting),
                    context.getRef(subFlow),
                    context.getRef(subFlow.getDeclaringType()),
                    NncUtils.map(arguments, Argument::toDTO)
            );
        }
    }

    public Value getSelfId() {
        return selfId;
    }

    public void setSelfId(Value selfId) {
        this.selfId = selfId;
    }

    protected Instance getSelf(MetaFrame frame) {
        return selfId.evaluate(frame);
    }


}
