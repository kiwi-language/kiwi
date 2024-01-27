package tech.metavm.flow;

import tech.metavm.entity.*;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.flow.rest.MethodCallNodeParam;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;

@EntityType("方法调用节点")
public class MethodCallNode extends CallNode {

    public static MethodCallNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
        MethodCallNodeParam param = nodeDTO.getParam();
        var method = context.getMethod(param.getFlowRef());
        FlowParsingContext parsingContext = FlowParsingContext.create(scope, prev, context);
        var self = NncUtils.get(param.getSelf(), s -> ValueFactory.create(s, parsingContext));
        List<Argument> arguments = NncUtils.biMap(
                method.getParameters(),
                param.getArguments(),
                (p, a) -> new Argument(a.tmpId(), p, ValueFactory.create(a.value(), parsingContext))
        );
        MethodCallNode node;
        if (nodeDTO.id() != null) {
            node = (MethodCallNode) context.getNode(nodeDTO.id());
            node.setSelf(self);
            node.setSubFlow(method);
            node.setArguments(arguments);
        } else
            node = new MethodCallNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), prev, scope, self, method, arguments);
        return node;
    }

    @ChildEntity("自身")
    @Nullable
    private Value self;

    public MethodCallNode(Long tmpId, String name, @Nullable String code, NodeRT prev, ScopeRT scope, Value self, Method method, List<Argument> arguments) {
        super(tmpId, name, code, prev, scope, method, arguments);
        this.self = NncUtils.get(self, s -> addChild(s, "self"));
    }

    @Override
    protected MethodCallNodeParam getParam(SerializeContext serializeContext) {
        try (var serContext = SerializeContext.enter()) {
            var method = getMethod();
            return new MethodCallNodeParam(
                    NncUtils.get(self, Value::toDTO),
                    serContext.getRef(method),
                    serContext.getRef(method.getDeclaringType()),
                    NncUtils.map(arguments, Argument::toDTO)
            );
        }
    }

    private Method getMethod() {
        return (Method) super.getSubFlow();
    }

    public @Nullable Value getSelf() {
        return self;
    }

    public void setSelf(Value self) {
        this.self = NncUtils.get(self, s -> addChild(s, "self"));
    }

    protected ClassInstance getSelf(MetaFrame frame) {
        return self != null ? (ClassInstance) self.evaluate(frame) : null;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitSubFlowNode(this);
    }
}
