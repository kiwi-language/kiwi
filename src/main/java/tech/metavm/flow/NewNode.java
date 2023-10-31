package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.flow.rest.CallParam;
import tech.metavm.flow.rest.NewParam;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.ClassType;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;

@EntityType("创建对象节点")
public class NewNode extends CallNode<NewParam> {

    public static NewNode create(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope, IEntityContext context) {
        CallParam param = nodeDTO.getParam();
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


    @ChildEntity("主对象")
    @Nullable
    private MasterRef master;

    // 临时对象：如果创建的对象未被其他持久对象引用，则该对象不会被持久化
    @EntityField("是否临时")
    private boolean ephemeral;

    public NewNode(Long tmpId, String name, Flow subFlow,
                   List<Argument> arguments,
                   NodeRT<?> prev, ScopeRT scope) {
        super(tmpId, name, prev, scope, arguments, subFlow);
    }

    @Override
    protected NewParam getParam(boolean persisting) {
        try (var context = SerializeContext.enter()) {
            return new NewParam(
                    context.getRef(subFlow),
                    context.getRef(subFlow.getDeclaringType()),
                    NncUtils.map(arguments, Argument::toDTO),
                    NncUtils.get(master, MasterRef::toDTO)
            );
        }
    }

    @Override
    protected void setExtraParam(CallParam paramDTO, IEntityContext context) {
        var newParam = (NewParam) paramDTO;
        var parsingContext = getParsingContext(context);
        if(newParam.getMaster() != null) {
            master = MasterRef.create(newParam.getMaster(), parsingContext, getType());
        }
    }

    @Override
    protected Instance getSelf(MetaFrame frame) {
        var instance = InstanceFactory.allocate(
                subFlow.getDeclaringType().getInstanceClass(), subFlow.getDeclaringType()
        );
        if (!ephemeral) {
            frame.getStack().getContext().bind(instance);
        }
        return instance;
    }

    @Override
    public void onReturn(Instance returnValue, MetaFrame frame) {
        if(master != null) {
            master.setChild(returnValue, frame);
        }
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
