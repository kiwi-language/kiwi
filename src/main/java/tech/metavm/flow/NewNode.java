package tech.metavm.flow;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.InstanceFactory;
import tech.metavm.entity.SerializeContext;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.flow.rest.CallParamDTO;
import tech.metavm.flow.rest.NewParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.Instance;
import tech.metavm.util.NncUtils;

import java.util.List;

@EntityType("创建对象节点")
public class NewNode extends CallNode<NewParamDTO> {

    public static NewNode create(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope, IEntityContext context) {
        CallParamDTO param = nodeDTO.getParam();
        Flow subFlow = getFlow(param, context);
        FlowParsingContext flowParsingContext = FlowParsingContext.create(scope, prev, context);
        List<FieldParam> arguments = NncUtils.map(
                param.getFields(), p -> new FieldParam(context.getField(p.fieldRef()), p.value(), flowParsingContext)
        );
        return new NewNode(nodeDTO.tmpId(), nodeDTO.name(), subFlow, arguments, prev, scope);
    }

    public NewNode(Long tmpId, String name, Flow subFlow, List<FieldParam> arguments, NodeRT<?> prev, ScopeRT scope) {
        super(tmpId, name, prev, scope, arguments, subFlow);
    }

    @Override
    protected NewParamDTO getParam(boolean persisting) {
        try(var context = SerializeContext.enter()) {
            return new NewParamDTO(
                    context.getRef(subFlow),
                    context.getRef(subFlow.getDeclaringType()),
                    NncUtils.map(arguments, arg -> arg.toDTO(persisting))
            );
        }
    }

    @Override
    protected Instance getSelf(FlowFrame frame) {
        var instance = InstanceFactory.allocate(
                subFlow.getDeclaringType().getInstanceClass(), subFlow.getDeclaringType()
        );
        frame.getStack().getContext().bind(instance);
        return instance;
    }

}
