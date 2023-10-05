package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.InstanceFactory;
import tech.metavm.entity.SerializeContext;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.flow.rest.CallParamDTO;
import tech.metavm.flow.rest.NewParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.ClassType;
import tech.metavm.util.NncUtils;

import java.util.List;

@EntityType("创建对象节点")
public class NewNode extends CallNode<NewParamDTO> {

    public static NewNode create(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope, IEntityContext context) {
        CallParamDTO param = nodeDTO.getParam();
        Flow subFlow = getFlow(param, context);
        FlowParsingContext parsingContext = FlowParsingContext.create(scope, prev, context);
        List<Argument> arguments = NncUtils.biMap(
                subFlow.getParameters(),
                param.getArguments(),
                (parameter, argDTO) -> new Argument(argDTO.tmpId(), parameter,
                        ValueFactory.create(argDTO.value(), parsingContext))
        );
        return new NewNode(nodeDTO.tmpId(), nodeDTO.name(), subFlow, arguments, prev, scope);
    }

    // 临时对象：如果创建的对象未被其他持久对象引用，则该对象不会被持久化
    private boolean ephemeral;

    public NewNode(Long tmpId, String name, Flow subFlow,
                   List<Argument> arguments,
                   NodeRT<?> prev, ScopeRT scope) {
        super(tmpId, name, prev, scope, arguments, subFlow);
    }

    @Override
    protected NewParamDTO getParam(boolean persisting) {
        try(var context = SerializeContext.enter()) {
            return new NewParamDTO(
                    context.getRef(subFlow),
                    context.getRef(subFlow.getDeclaringType()),
                    NncUtils.map(arguments, Argument::toDTO)
            );
        }
    }

    @Override
    protected Instance getSelf(FlowFrame frame) {
        var instance = InstanceFactory.allocate(
                subFlow.getDeclaringType().getInstanceClass(), subFlow.getDeclaringType()
        );
        if(!ephemeral) {
            frame.getStack().getContext().bind(instance);
        }
        return instance;
    }

    @Override
    @NotNull
    public ClassType getType() {
        return (ClassType) NncUtils.requireNonNull(super.getType());
    }

    public boolean isEphemeral() {
        return ephemeral;
    }

    public void setEphemeral(boolean ephemeral) {
        this.ephemeral = ephemeral;
    }

}
