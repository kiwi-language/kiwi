package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.flow.rest.CallParam;
import tech.metavm.flow.rest.NewObjectParam;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.ClassType;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;

@EntityType("创建对象节点")
public class NewObjectNode extends CallNode<NewObjectParam> implements NewNode{

    public static NewObjectNode create(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope, IEntityContext context) {
        NewObjectParam param = nodeDTO.getParam();
        Flow subFlow = getFlow(param, context);
        FlowParsingContext parsingContext = FlowParsingContext.create(scope, prev, context);
        List<Argument> arguments = NncUtils.biMap(
                subFlow.getParameters(),
                param.getArguments(),
                (parameter, argDTO) -> new Argument(argDTO.tmpId(), parameter,
                        ValueFactory.create(argDTO.value(), parsingContext))
        );
        var parentRef = NncUtils.get(param.getParent(),
                p ->ParentRef.create(p, parsingContext, subFlow.getReturnType()));
        return new NewObjectNode(nodeDTO.tmpId(), nodeDTO.name(), subFlow, arguments, prev, scope, parentRef);
    }


    @ChildEntity("父对象")
    @Nullable
    private ParentRef parent;

    // 临时对象：如果创建的对象未被其他持久对象引用，则该对象不会被持久化
    @EntityField("是否临时")
    private boolean ephemeral;

    public NewObjectNode(Long tmpId, String name, Flow subFlow,
                         List<Argument> arguments,
                         NodeRT<?> prev, ScopeRT scope, @Nullable ParentRef parentRef) {
        super(tmpId, name, prev, scope, arguments, subFlow);
        setParent(parentRef);
    }

    @Override
    protected NewObjectParam getParam(boolean persisting) {
        try (var context = SerializeContext.enter()) {
            return new NewObjectParam(
                    context.getRef(subFlow),
                    context.getRef(subFlow.getDeclaringType()),
                    NncUtils.map(arguments, Argument::toDTO),
                    NncUtils.get(parent, ParentRef::toDTO)
            );
        }
    }

    @Override
    protected void setExtraParam(CallParam paramDTO, IEntityContext context) {
        var newParam = (NewObjectParam) paramDTO;
        var parsingContext = getParsingContext(context);
        if(newParam.getParent() != null) {
            parent = ParentRef.create(newParam.getParent(), parsingContext, getType());
        }
    }

    @Override
    protected Instance getSelf(MetaFrame frame) {
        var instance = new ClassInstance(subFlow.getDeclaringType(), NncUtils.get(parent, p -> p.evaluate(frame)));
        if (!ephemeral) {
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

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitNewObjectNode(this);
    }

    @Override
    public void setParent(@Nullable ParentRef parentRef) {
        this.parent = parentRef;
    }

    @Override
    public @Nullable ParentRef getParentRef() {
        return parent;
    }
}
