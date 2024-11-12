package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.FunctionCallNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.TypeParser;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.Objects;

@EntityType
public class FunctionCallNode extends CallNode {

    public static FunctionCallNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        var node = (FunctionCallNode) context.getNode(Id.parse(nodeDTO.id()));
        if(node == null) {
            var param = (FunctionCallNodeParam) nodeDTO.param();
            var functionRef = FunctionRef.create(Objects.requireNonNull(param.getFlowRef()), context);
            node = new FunctionCallNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope, functionRef);
            node.setCapturedVariableTypes(NncUtils.map(param.getCapturedVariableTypes(), t -> TypeParser.parseType(t, context)));
            node.setCapturedVariableIndexes(param.getCapturedVariableIndexes());
        }
        return node;
    }

    public FunctionCallNode(Long tmpId, String name, NodeRT prev, ScopeRT scope, FunctionRef functionRef) {
        super(tmpId, name,  prev, scope, functionRef);
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
                NncUtils.map(capturedVariableTypes, t -> t.toExpression(serializeContext)),
                capturedVariableIndexes.toList()
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
        else
            throw new InternalException("Invalid sub flow for function call node: " + flowRef);
    }
}
