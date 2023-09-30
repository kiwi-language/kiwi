package tech.metavm.flow;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.SerializeContext;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.expression.ParsingContext;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.flow.rest.SubFlowParam;
import tech.metavm.object.instance.Instance;
import tech.metavm.util.NncUtils;

import java.util.List;

@EntityType("子流程节点")
public class SubFlowNode extends CallNode<SubFlowParam> {

    public static SubFlowNode create(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope, IEntityContext context) {
        SubFlowParam param = nodeDTO.getParam();
        FlowParsingContext flowParsingContext = FlowParsingContext.create(scope, prev, context);
        Value self = ValueFactory.create(param.getSelf(), flowParsingContext);
        List<FieldParam> arguments = NncUtils.map(
                param.getFields(), p -> new FieldParam(context.getField(p.fieldRef()), p.value(), flowParsingContext)
        );
        return new SubFlowNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope, self, arguments,
                getFlow(param, context));
    }

    @ChildEntity("目标对象")
    private Value selfId;

    public SubFlowNode(Long tmpId, String name, NodeRT<?> prev, ScopeRT scope, Value selfId, List<FieldParam> arguments,
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
                    NncUtils.map(arguments, fp -> fp.toDTO(persisting))
            );
        }
    }

    public Value getSelfId() {
        return selfId;
    }

    public Flow getSubFlow() {
        return subFlow;
    }

    public void setSelfId(Value selfId) {
        this.selfId = selfId;
    }

    public void setSubFlow(Flow flow) {
        this.subFlow = flow;
    }

    public void setArguments(List<FieldParam> arguments) {
        this.arguments.clear();
        this.arguments.addAll(arguments);
    }

    protected Instance getSelf(FlowFrame frame) {
        return selfId.evaluate(frame);
    }


}
