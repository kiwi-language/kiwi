package tech.metavm.flow;

import tech.metavm.entity.ElementVisitor;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.SerializeContext;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.flow.rest.FunctionCallNodeParam;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.type.CompositeTypeFacadeImpl;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.Types;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;

@EntityType("函数调用节点")
public class FunctionCallNode extends CallNode {

    public static FunctionCallNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
        var param = (FunctionCallNodeParam) nodeDTO.param();
        var node = (FunctionCallNode) context.getNode(Id.parse(nodeDTO.id()));
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        var function = context.getFunction(Id.parse(param.getFlowId()));
        List<Argument> arguments = NncUtils.biMap(
                function.getParameters(),
                param.getArguments(),
                (parameter, argDTO) -> new Argument(argDTO.tmpId(), parameter,
                        ValueFactory.create(argDTO.value(), parsingContext))
        );
        if(node == null) {
            var outputType = function.getReturnType().isVoid() ? null
                    : Types.tryCapture(function.getReturnType(), scope.getFlow(), CompositeTypeFacadeImpl.fromContext(context));
            return new FunctionCallNode(
                    nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), outputType,
                    prev, scope, function, arguments
            );
        }
        else {
            node.setSubFlow(function);
            node.setArguments(arguments);
        }
        return node;
    }

    public FunctionCallNode(Long tmpId, String name, @Nullable String code, @Nullable Type outputType, NodeRT prev, ScopeRT scope, Function subFlow, List<Argument> arguments) {
        super(tmpId, name, code, outputType, prev, scope, subFlow, arguments);
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
                serializeContext.getId(getSubFlow()),
                null,
                NncUtils.map(arguments, Argument::toDTO)
        );
    }

    @Override
    public Function getSubFlow() {
        return (Function) super.getSubFlow();
    }

    @Override
    public void setSubFlow(Flow subFlow) {
        if(subFlow instanceof Function)
            super.setSubFlow(subFlow);
        else
            throw new InternalException("Invalid sub flow for function call node: " + subFlow);
    }
}
