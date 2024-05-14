package tech.metavm.flow;

import tech.metavm.entity.*;
import tech.metavm.expression.ExpressionParser;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.expression.VarType;
import tech.metavm.flow.rest.MethodCallNodeParam;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.TypeParser;
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
            var methodRef = MethodRef.create(Objects.requireNonNull(param.getFlowRef()), context);
            var self = NncUtils.get(param.getSelf(), s -> ValueFactory.create(s, parsingContext));
            List<Argument> arguments = NncUtils.biMap(
                    methodRef.resolve().getParameters(),
                    param.getArguments(),
                    (p, a) -> new Argument(a.tmpId(), p.getRef(), ValueFactory.create(a.value(), parsingContext))
            );
            //noinspection DuplicatedCode
            var node = saveNode0(nodeDTO, self, methodRef, arguments, prev, scope, context);
            node.setCapturedExpressionTypes(NncUtils.map(param.getCapturedExpressionTypes(), t -> TypeParser.parse(t, context)));
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
                declaringType = (ClassType) TypeParser.parse(Objects.requireNonNull(param.getType()), context);
            var argumentValues = NncUtils.map(
                    param.getArgumentValues(),
                    arg -> ValueFactory.create(arg, parsingContext)
            );
            var argumentTypes = NncUtils.map(argumentValues, Value::getType);
            var klass = declaringType.resolve();
            var method = klass.resolveMethod(param.getFlowCode(),
                    argumentTypes,
                    NncUtils.map(param.getTypeArguments(), context::getType),
                    isStatic);
            if (NncUtils.isNotEmpty(param.getTypeArguments())) {
                method = method.getParameterized(NncUtils.map(param.getTypeArguments(), context::getType));
            }
            var methodRef = method.getRef();
            var arguments = new ArrayList<Argument>();
            NncUtils.biForEach(methodRef.getRawFlow().getParameters(), argumentValues, (p, v) ->
                    arguments.add(new Argument(null, p.getRef(), v))
            );
            return saveNode0(nodeDTO, self, methodRef, arguments, prev, scope, context);
        }
    }

    private static MethodCallNode saveNode0(NodeDTO nodeDTO, Value self,
                                            MethodRef methodRef,
                                            List<Argument> arguments,
                                            NodeRT prev,
                                            ScopeRT scope,
                                            IEntityContext context) {
        var node = (MethodCallNode) context.getNode(nodeDTO.id());
        if (node != null) {
            node.setSelf(self);
            node.setFlowRef(methodRef);
            node.setArguments(arguments);
        } else {
            node = new MethodCallNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), prev, scope, self, methodRef, arguments);
        }
        return node;
    }

    @ChildEntity("自身")
    @Nullable
    private Value self;

    public MethodCallNode(Long tmpId,
                          String name,
                          @Nullable String code,
                          NodeRT prev,
                          ScopeRT scope,
                          Value self,
                          MethodRef methodRef,
                          List<Argument> arguments) {
        super(tmpId, name, code, prev, scope, methodRef, arguments);
        this.self = NncUtils.get(self, s -> addChild(s, "self"));
    }

    @Override
    protected MethodCallNodeParam getParam(SerializeContext serializeContext) {
        var method = getMethod();
        return new MethodCallNodeParam(
                NncUtils.get(self, Value::toDTO),
                getFlowRef().toDTO(serializeContext),
                null,
                null,
                method.getDeclaringType().getType().toExpression(serializeContext),
                NncUtils.map(arguments, Argument::toDTO),
                null,
                NncUtils.map(capturedExpressionTypes, t -> t.toExpression(serializeContext)),
                NncUtils.map(capturedExpressions, e -> e.build(VarType.NAME))
        );
    }

    @Override
    public MethodRef getFlowRef() {
        return (MethodRef) super.getFlowRef();
    }

    @Override
    public void writeContent(CodeWriter writer) {
        var method = getFlowRef().resolve();
        writer.write(method.getDeclaringType().getName() + "." + method.getNameWithTypeArguments()
                + "(" + NncUtils.join(arguments, Argument::getText, ", ") + ")");
    }

    private Method getMethod() {
        return (Method) super.getFlowRef().resolve();
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
