package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.flow.rest.CallParamDTO;
import tech.metavm.flow.rest.NewParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;

import java.util.Collections;
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

    @EntityField("类类型实参")
    private final Table<Type> classTypeArguments = new Table<>(Type.class);

    /*
    临时对象：如果创建的对象未被其他持久对象引用，则该对象不会被持久化
     */
    private boolean ephemeral;

    public NewNode(Long tmpId, String name, Flow subFlow,
                   List<FieldParam> arguments,
                   NodeRT<?> prev, ScopeRT scope) {
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
        if(!ephemeral) {
            frame.getStack().getContext().bind(instance);
        }
        return instance;
    }

    @Override
    @NotNull
    public ClassType getType() {
        return (ClassType) super.getType();
    }

    public boolean isEphemeral() {
        return ephemeral;
    }

    public void setEphemeral(boolean ephemeral) {
        this.ephemeral = ephemeral;
    }

    public List<Type> getClassTypeArguments() {
        return Collections.unmodifiableList(classTypeArguments);
    }
}
