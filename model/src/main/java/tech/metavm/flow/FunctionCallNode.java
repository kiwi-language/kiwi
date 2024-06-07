package tech.metavm.flow;

import tech.metavm.entity.ElementVisitor;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.SerializeContext;
import tech.metavm.expression.ExpressionParser;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.expression.VarType;
import tech.metavm.flow.rest.FunctionCallNodeParam;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.type.TypeParser;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

@EntityType
public class FunctionCallNode extends CallNode {

    public static FunctionCallNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
        var param = (FunctionCallNodeParam) nodeDTO.param();
        var node = (FunctionCallNode) context.getNode(Id.parse(nodeDTO.id()));
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        var functionRef = FunctionRef.create(Objects.requireNonNull(param.getFlowRef()), context);
        var function = functionRef.resolve();
        List<Argument> arguments = NncUtils.biMap(
                function.getParameters(),
                param.getArguments(),
                (parameter, argDTO) -> new Argument(argDTO.tmpId(), parameter.getRef(),
                        ValueFactory.create(argDTO.value(), parsingContext))
        );
        if(node == null) {
            return new FunctionCallNode(
                    nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(),
                    prev, scope, functionRef, arguments
            );
        }
        else {
            node.setFlowRef(functionRef);
            node.setArguments(arguments);
        }
        node.setCapturedExpressionTypes(NncUtils.map(param.getCapturedExpressionTypes(), t -> TypeParser.parseType(t, context)));
        node.setCapturedExpressions(NncUtils.map(param.getCapturedExpressions(), e -> ExpressionParser.parse(e, parsingContext)));
        return node;
    }

    public FunctionCallNode(Long tmpId, String name, @Nullable String code, NodeRT prev, ScopeRT scope, FunctionRef functionRef, List<Argument> arguments) {
        super(tmpId, name, code, prev, scope, functionRef, arguments);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitFunctionCallNode(this);
    }

    @Override
    protected @Nullable ClassInstance getSelf(MetaFrame frame) {
        return null;
    }

    @Override
    protected FunctionCallNodeParam getParam(SerializeContext serializeContext) {
        return new FunctionCallNodeParam(
                getFlowRef().toDTO(serializeContext),
                null,
                NncUtils.map(arguments, Argument::toDTO),
                NncUtils.map(capturedExpressionTypes, t -> t.toExpression(serializeContext)),
                NncUtils.map(capturedExpressions, e -> e.build(VarType.NAME))
        );
    }

    @Override
    public FunctionRef getFlowRef() {
        return (FunctionRef) super.getFlowRef();
    }

    @Override
    public void setFlowRef(FlowRef flowRef) {
        if(flowRef instanceof FunctionRef)
            super.setFlowRef(flowRef);
        throw new InternalException("Invalid sub flow for function call node: " + flowRef);
    }
}
