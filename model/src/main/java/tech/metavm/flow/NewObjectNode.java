package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.*;
import tech.metavm.expression.ExpressionParser;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.expression.VarType;
import tech.metavm.flow.rest.NewObjectNodeParam;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.ClassInstanceBuilder;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Type;
import tech.metavm.util.DebugEnv;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@EntityType("创建对象节点")
public class NewObjectNode extends CallNode implements NewNode {

    public static final Logger logger = LoggerFactory.getLogger(NewObjectNode.class);

    public static NewObjectNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
        NewObjectNodeParam param = nodeDTO.getParam();
        if (param.isResolved()) {
            var subFlow = (Method) getFlow(param, context);
            var parsingContext = FlowParsingContext.create(scope, prev, context);
            List<Argument> arguments = NncUtils.biMap(
                    subFlow.getParameters(),
                    param.getArguments(),
                    (parameter, argDTO) -> new Argument(argDTO.tmpId(), parameter,
                            ValueFactory.create(argDTO.value(), parsingContext))
            );
            var parentRef = NncUtils.get(param.getParent(),
                    p -> ParentRef.create(p, parsingContext, context, subFlow.getReturnType()));
            //noinspection DuplicatedCode
            var node = saveNode0(nodeDTO, subFlow, parentRef, arguments, prev, scope, context);
            node.setCapturedExpressionTypes(NncUtils.map(param.getCapturedExpressionTypeIds(), context::getType));
            node.setCapturedExpressions(NncUtils.map(param.getCapturedExpressions(), e -> ExpressionParser.parse(e, parsingContext)));
            return node;
        } else {
            var declaringType = context.getClassType(param.getTypeId());
            var parsingContext = FlowParsingContext.create(scope, prev, context);
            var argumentValues = NncUtils.map(param.getArgumentValues(), arg -> ValueFactory.create(arg, parsingContext));
            var constructor = declaringType.resolveMethod(param.getFlowCode(),
                    NncUtils.map(argumentValues, Value::getType),
                    NncUtils.map(param.getTypeArgumentIds(), context::getType),
                    false,
                    context.getGenericContext());
            if (NncUtils.isNotEmpty(param.getArguments()))
                constructor = context.getGenericContext().getParameterizedFlow(constructor, NncUtils.map(param.getTypeArgumentIds(), context::getType));
            var arguments = new ArrayList<Argument>();
            try {
                NncUtils.biForEach(constructor.getParameters(), argumentValues, (p, v) ->
                        arguments.add(new Argument(null, p, v))
                );
            } catch (RuntimeException e) {
                logger.info("error constructor: " + constructor.getName() + "(" + NncUtils.join(constructor.getParameterTypes(), Type::getTypeDesc) + ")");
                throw e;
            }
            return saveNode0(nodeDTO, constructor, null, arguments, prev, scope, context);
        }
    }

    private static NewObjectNode saveNode0(NodeDTO nodeDTO, Method subFlow, ParentRef parentRef, List<Argument> arguments,
                                           NodeRT prev, ScopeRT scope, IEntityContext context) {
        NewObjectNodeParam param = nodeDTO.getParam();
        var node = (NewObjectNode) context.getNode(nodeDTO.id());
        if (node != null) {
            node.setSubFlow(subFlow);
            node.setParentRef(parentRef);
            node.setArguments(arguments);
            node.setEphemeral(param.isEphemeral());
            node.setUnbound(param.isUnbound());
        } else
            node = new NewObjectNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), subFlow,
                    arguments, prev, scope, parentRef, param.isEphemeral(), param.isUnbound());
        return node;
    }

    @ChildEntity("父引用")
    @Nullable
    private ParentRef parentRef;

    // 临时对象：如果对象未被其他持久对象引用，则不会被持久化
    @EntityField("是否临时")
    private boolean ephemeral;

    @EntityField("是否未绑定")
    private boolean unbound;

    public NewObjectNode(Long tmpId, String name, @Nullable String code, Flow subFlow,
                         List<Argument> arguments,
                         NodeRT prev, ScopeRT scope,
                         @Nullable ParentRef parentRef, boolean ephemeral, boolean unbound) {
        super(tmpId, name, code, subFlow.getReturnType(), prev, scope, subFlow, arguments);
        setParentRef(parentRef);
        this.ephemeral = ephemeral;
        this.unbound = unbound;
    }

    @Override
    protected NewObjectNodeParam getParam(SerializeContext serializeContext) {
        try (var serContext = SerializeContext.enter()) {
            return new NewObjectNodeParam(
                    serContext.getId(getSubFlow()),
                    null,
                    null,
                    serContext.getId(getSubFlow().getDeclaringType()),
                    NncUtils.map(arguments, Argument::toDTO),
                    null,
                    NncUtils.get(parentRef, ParentRef::toDTO),
                    ephemeral,
                    unbound,
                    NncUtils.map(capturedExpressionTypes, serContext::getId),
                    NncUtils.map(capturedExpressions, e -> e.build(VarType.NAME))
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
        if (DebugEnv.debugging)
            DebugEnv.logger.info("getSelf for node {}, ephemeral: {}, unbound: {}", this.getName(), instance.isEphemeral(), unbound);
        if (!instance.isEphemeral() && !unbound)
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
        return (ClassType) getSubFlow().getReturnType();
    }

    @Override
    public void writeContent(CodeWriter writer) {
        super.writeContent(writer);
        if (ephemeral)
            writer.write(" ephemeral");
        if (parentRef != null)
            writer.write(" " + parentRef.getText());
    }

    public boolean isEphemeral() {
        return ephemeral;
    }

    public void setEphemeral(boolean ephemeral) {
        this.ephemeral = ephemeral;
    }

    public void setUnbound(boolean unbound) {
        this.unbound = unbound;
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
