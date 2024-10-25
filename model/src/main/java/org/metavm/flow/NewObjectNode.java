package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.expression.ExpressionParser;
import org.metavm.expression.FlowParsingContext;
import org.metavm.expression.VarType;
import org.metavm.flow.rest.NewObjectNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.ClassInstanceBuilder;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeParser;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@EntityType
public class NewObjectNode extends CallNode implements NewNode {

    public static final Logger logger = LoggerFactory.getLogger(NewObjectNode.class);

    public static NewObjectNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        NewObjectNodeParam param = nodeDTO.getParam();
        if (param.isResolved()) {
            var declaringType = (ClassType) TypeParser.parseType(Objects.requireNonNull(param.getType()), context);
            var methodRef = MethodRef.create(Objects.requireNonNull(param.getFlowRef()), context);
            var parsingContext = FlowParsingContext.create(scope, prev, context);
            List<Argument> arguments = NncUtils.biMap(
                    methodRef.resolve().getParameters(),
                    param.getArguments(),
                    (parameter, argDTO) -> new Argument(argDTO.tmpId(), parameter.getRef(),
                            ValueFactory.create(argDTO.value(), parsingContext))
            );
            var parentRef = NncUtils.get(param.getParent(),
                    p -> ParentRef.create(p, parsingContext, context, declaringType));
            //noinspection DuplicatedCode
            var node = saveNode0(nodeDTO, methodRef, parentRef, arguments, prev, scope, context);
            node.setCapturedExpressionTypes(NncUtils.map(param.getCapturedExpressionTypes(), t -> TypeParser.parseType(t, context)));
            node.setCapturedExpressions(NncUtils.map(param.getCapturedExpressions(), e -> ExpressionParser.parse(e, parsingContext)));
            return node;
        } else {
            var declaringType = ((ClassType) TypeParser.parseType(Objects.requireNonNull(param.getType()), context)).resolve();
            var parsingContext = FlowParsingContext.create(scope, prev, context);
            var argumentValues = NncUtils.map(param.getArgumentValues(), arg -> ValueFactory.create(arg, parsingContext));
            var constructor = declaringType.resolveMethod(param.getFlowCode(),
                    NncUtils.map(argumentValues, Value::getType),
                    NncUtils.map(param.getTypeArguments(), context::getType),
                    false);
            if (NncUtils.isNotEmpty(param.getArguments()))
                constructor = constructor.getParameterized(NncUtils.map(param.getTypeArguments(), context::getType));
            var arguments = new ArrayList<Argument>();
            var constructorRef = constructor.getRef();
            try {
                NncUtils.biForEach(constructorRef.getRawFlow().getParameters(), argumentValues, (p, v) ->
                        arguments.add(new Argument(null, p.getRef(), v))
                );
            } catch (RuntimeException e) {
                logger.info("error constructor: " + constructor.getName() + "(" + NncUtils.join(constructor.getParameterTypes(), Type::getTypeDesc) + ")");
                throw e;
            }
            return saveNode0(nodeDTO, constructorRef, null, arguments, prev, scope, context);
        }
    }

    private static NewObjectNode saveNode0(NodeDTO nodeDTO, MethodRef methodRef, ParentRef parentRef, List<Argument> arguments,
                                           NodeRT prev, ScopeRT scope, IEntityContext context) {
        NewObjectNodeParam param = nodeDTO.getParam();
        var node = (NewObjectNode) context.getNode(nodeDTO.id());
        if (node != null) {
            node.setFlowRef(methodRef);
            node.setParentRef(parentRef);
            node.setArguments(arguments);
            node.setEphemeral(param.isEphemeral());
            node.setUnbound(param.isUnbound());
        } else
            node = new NewObjectNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), methodRef,
                    arguments, prev, scope, parentRef, param.isEphemeral(), param.isUnbound());
        return node;
    }

    @Nullable
    private ParentRef parentRef;

    private boolean ephemeral;

    private boolean unbound;

    public NewObjectNode(Long tmpId, String name, @Nullable String code, MethodRef methodRef,
                         List<Argument> arguments,
                         NodeRT prev, ScopeRT scope,
                         @Nullable ParentRef parentRef, boolean ephemeral, boolean unbound) {
        super(tmpId, name, code, prev, scope, methodRef, arguments);
        setParentRef(parentRef);
        this.ephemeral = ephemeral;
        this.unbound = unbound;
    }

    @Override
    protected NewObjectNodeParam getParam(SerializeContext serializeContext) {
        return new NewObjectNodeParam(
                getFlowRef().toDTO(serializeContext),
                null,
                null,
                getFlowRef().getDeclaringType().toExpression(serializeContext),
                NncUtils.map(arguments, Argument::toDTO),
                null,
                NncUtils.get(parentRef, ParentRef::toDTO),
                ephemeral,
                unbound,
                NncUtils.map(capturedExpressionTypes, t -> t.toExpression(serializeContext)),
                NncUtils.map(capturedExpressions, e -> e.build(VarType.NAME))
        );
    }

    @Override
    protected ClassInstance getSelf(MetaFrame frame) {
        var methodRef = getFlowRef();
        var type = methodRef.getDeclaringType();
        var parentRef = NncUtils.get(this.parentRef, p -> p.evaluate(frame));
        var instance = ClassInstanceBuilder.newBuilder(type)
                .ephemeral(ephemeral)
                .parentRef(parentRef)
                .build();
        if (!instance.isEphemeral() && !unbound)
            frame.addInstance(instance);
        return instance;
    }

    @Override
    public MethodRef getFlowRef() {
        return (MethodRef) super.getFlowRef();
    }

    @Override
    public void setFlowRef(FlowRef flowRef) {
        if (flowRef instanceof MethodRef)
            super.setFlowRef(flowRef);
        else
            throw new InternalException("Invalid subflow for NewObjectNode: " + flowRef);
    }

    @Override
    @NotNull
    public ClassType getType() {
        return (ClassType) super.getType();
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
