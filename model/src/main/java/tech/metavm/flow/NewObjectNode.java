package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.entity.natives.RuntimeExceptionNative;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.flow.rest.NewObjectNodeParam;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.ClassInstanceBuilder;
import tech.metavm.object.type.ClassType;
import tech.metavm.util.Instances;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;

@EntityType("创建对象节点")
public class NewObjectNode extends CallNode implements NewNode {

    public static NewObjectNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
        NewObjectNodeParam param = nodeDTO.getParam();
        Flow subFlow = getFlow(param, context);
        FlowParsingContext parsingContext = FlowParsingContext.create(scope, prev, context);
        List<Argument> arguments = NncUtils.biMap(
                subFlow.getParameters(),
                param.getArguments(),
                (parameter, argDTO) -> new Argument(argDTO.tmpId(), parameter,
                        ValueFactory.create(argDTO.value(), parsingContext))
        );
        var parentRef = NncUtils.get(param.getParent(),
                p -> ParentRef.create(p, parsingContext, context, subFlow.getReturnType()));
        NewObjectNode node;
        if (nodeDTO.id() != null) {
            node = (NewObjectNode) context.getNode(nodeDTO.id());
            node.setSubFlow(subFlow);
            node.setArguments(arguments);
            node.setParentRef(parentRef);
        } else
            node = new NewObjectNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), subFlow,
                    arguments, prev, scope, parentRef, param.isEphemeral());
        return node;
    }

    @ChildEntity("父引用")
    @Nullable
    private ParentRef parentRef;

    // 临时对象：如果对象未被其他持久对象引用，则不会被持久化
    @EntityField("是否临时")
    private boolean ephemeral;

    public NewObjectNode(Long tmpId, String name, @Nullable String code, Flow subFlow,
                         List<Argument> arguments,
                         NodeRT prev, ScopeRT scope,
                         @Nullable ParentRef parentRef, boolean ephemeral) {
        super(tmpId, name, code, prev, scope, subFlow, arguments);
        setParentRef(parentRef);
        this.ephemeral = ephemeral;
    }

    @Override
    protected NewObjectNodeParam getParam(SerializeContext serializeContext) {
        try (var serContext = SerializeContext.enter()) {
            return new NewObjectNodeParam(
                    serContext.getRef(getSubFlow()),
                    serContext.getRef(getSubFlow().getDeclaringType()),
                    NncUtils.map(arguments, Argument::toDTO),
                    NncUtils.get(parentRef, ParentRef::toDTO),
                    ephemeral
            );
        }
    }

    @Override
    protected ClassInstance getSelf(MetaFrame frame) {
        var subFlow = getSubFlow();
        var type = subFlow.getDeclaringType();
        var parentRef = NncUtils.get(this.parentRef, p -> p.evaluate(frame));
        var instance = ClassInstanceBuilder.newBuilder(type)
                .ephemeral(ephemeral)
                .parentRef(parentRef)
                .build();
        if (!instance.isEphemeral())
            frame.addInstance(instance);
        return instance;
    }

    @Override
    public Method getSubFlow() {
        return (Method) super.getSubFlow();
    }

    @Override
    public void setSubFlow(Flow subFlow) {
        if (subFlow instanceof Method)
            super.setSubFlow(subFlow);
        else
            throw new InternalException("Invalid subflow for NewObjectNode: " + subFlow);
    }

    @Override
    @NotNull
    public ClassType getType() {
        return (ClassType) NncUtils.requireNonNull(super.getType());
    }

    @Override
    public void writeContent(CodeWriter writer) {
        super.writeContent(writer);
        if (ephemeral)
            writer.write(" ephemeral");
        if (parentRef != null)
            writer.write(" " + parentRef.getText());
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        var result = super.execute(frame);
        if (result.output() != null) {
            var instance = (ClassInstance) result.output();
            var uninitializedField = instance.findUninitializedField();
            if (uninitializedField != null) {
                var exception = ClassInstance.allocate(StandardTypes.getRuntimeExceptionType());
                var exceptionNative = new RuntimeExceptionNative(exception);
                exceptionNative.RuntimeException(Instances.stringInstance(
                                "Fail to construct object " + instance.getType().getName() + "，"
                                        + "field" + uninitializedField.getName() + " not initialized."),
                        frame.getNativeCallContext());
                return NodeExecResult.exception(exception);
            }
        }
        return result;
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
    public void setParentRef(@Nullable ParentRef parentRef) {
        this.parentRef = NncUtils.get(parentRef, p -> addChild(p, "parentRef"));
    }

    @Override
    public @Nullable ParentRef getParentRef() {
        return parentRef;
    }
}
