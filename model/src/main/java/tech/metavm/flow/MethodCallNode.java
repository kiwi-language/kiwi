package tech.metavm.flow;

import tech.metavm.entity.*;
import tech.metavm.expression.ExpressionParser;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.expression.VarType;
import tech.metavm.flow.rest.MethodCallNodeParam;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.type.*;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@EntityType("方法调用节点")
public class MethodCallNode extends CallNode {

    public static MethodCallNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
        MethodCallNodeParam param = nodeDTO.getParam();
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        if (param.isResolved()) {
            var method = context.getMethod(Id.parse(param.getFlowId()));
            var self = NncUtils.get(param.getSelf(), s -> ValueFactory.create(s, parsingContext));
            List<Argument> arguments = NncUtils.biMap(
                    method.getParameters(),
                    param.getArguments(),
                    (p, a) -> new Argument(a.tmpId(), p, ValueFactory.create(a.value(), parsingContext))
            );
            //noinspection DuplicatedCode
            var node = saveNode0(nodeDTO, self, method, arguments, prev, scope, context);
            node.setCapturedExpressionTypes(NncUtils.map(param.getCapturedExpressionTypeIds(), context::getType));
            node.setCapturedExpressions(NncUtils.map(param.getCapturedExpressions(), e -> ExpressionParser.parse(e, parsingContext)));
            return node;
        } else {
            var isStatic = param.getSelf() == null;
            var self = NncUtils.get(param.getSelf(), s -> ValueFactory.create(s, parsingContext));
            ClassType declaringType;
            if (self != null) {
                var exprTypes = prev != null ? prev.getExpressionTypes() : scope.getExpressionTypes();
                declaringType = (ClassType) exprTypes.getType(self.getExpression());
            } else
                declaringType = (ClassType) TypeParser.parse(Objects.requireNonNull(param.getTypeId()), new ContextTypeDefRepository(context));
            var argumentValues = NncUtils.map(
                    param.getArgumentValues(),
                    arg -> ValueFactory.create(arg, parsingContext)
            );
            var argumentTypes = NncUtils.map(argumentValues, Value::getType);
            var klass = declaringType.resolve();
            var method = klass.resolveMethod(param.getFlowCode(),
                    argumentTypes,
                    NncUtils.map(param.getTypeArgumentIds(), context::getType),
                    isStatic,
                    context.getGenericContext());
            if (NncUtils.isNotEmpty(param.getTypeArgumentIds())) {
                method = context.getGenericContext().getParameterizedFlow(method, NncUtils.map(param.getTypeArgumentIds(), context::getType));
            }
            var arguments = new ArrayList<Argument>();
            NncUtils.biForEach(method.getParameters(), argumentValues, (p, v) ->
                    arguments.add(new Argument(null, p, v))
            );
            return saveNode0(nodeDTO, self, method, arguments, prev, scope, context);
        }
    }

    private static MethodCallNode saveNode0(NodeDTO nodeDTO, Value self,
                                            Method method,
                                            List<Argument> arguments,
                                            NodeRT prev,
                                            ScopeRT scope,
                                            IEntityContext context) {
        var node = (MethodCallNode) context.getNode(nodeDTO.id());
        if (node != null) {
            node.setSelf(self);
            node.setSubFlow(method);
            node.setArguments(arguments);
        } else {
            var outputType = NncUtils.getOrElse(
                    nodeDTO.outputTypeId(),
                    context::getType,
                    method.getReturnType().isVoid() ? null : method.getReturnType()
            );
            node = new MethodCallNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), outputType, prev, scope, self, method, arguments);
        }
        return node;
    }

    @ChildEntity("自身")
    @Nullable
    private Value self;

    public MethodCallNode(Long tmpId,
                          String name,
                          @Nullable String code,
                          @Nullable Type outputType,
                          NodeRT prev,
                          ScopeRT scope,
                          Value self,
                          Method method,
                          List<Argument> arguments) {
        super(tmpId, name, code, outputType, prev, scope, method, arguments);
        NncUtils.requireTrue(method.getReturnType().isVoid() == (outputType == null));
        this.self = NncUtils.get(self, s -> addChild(s, "self"));
    }

    @Override
    protected MethodCallNodeParam getParam(SerializeContext serializeContext) {
        try (var serContext = SerializeContext.enter()) {
            var method = getMethod();
            return new MethodCallNodeParam(
                    NncUtils.get(self, Value::toDTO),
                    serContext.getId(method),
                    null,
                    null,
                    serContext.getId(method.getDeclaringType()),
                    NncUtils.map(arguments, Argument::toDTO),
                    null,
                    NncUtils.map(capturedExpressionTypes, serContext::getId),
                    NncUtils.map(capturedExpressions, e -> e.build(VarType.NAME))
            );
        }
    }

    @Override
    public Method getSubFlow() {
        return (Method) super.getSubFlow();
    }

    @Override
    public void writeContent(CodeWriter writer) {
        var method = getSubFlow();
        writer.write(method.getDeclaringType().getName() + "." + method.getNameWithTypeArguments()
                + "(" + NncUtils.join(arguments, Argument::getText, ", ") + ")");
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
