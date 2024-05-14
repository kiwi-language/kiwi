package tech.metavm.flow;

import tech.metavm.entity.ElementVisitor;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.SerializeContext;
import tech.metavm.expression.Expressions;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.flow.rest.WhileNodeParam;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Klass;
import tech.metavm.object.type.TypeParser;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;

@EntityType("While循环节点")
public class WhileNode extends LoopNode {

    public static WhileNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
        var outputType = ((ClassType) TypeParser.parse(nodeDTO.outputType(), context)).resolve();
        var condition = Values.expression(Expressions.trueExpression());
        // IMPORTANT COMMENT DON"T REMOVE:
        // DO NOT call setLoopParam here. setLoopParam should be called after the loop body has been constructed.
        // See FlowManager.saveLoopNodeContent
        WhileNode node = (WhileNode) context.getNode(nodeDTO.id());
        if (node == null)
            node = new WhileNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), outputType, prev, scope, condition);
        return node;
    }

    public WhileNode(Long tmpId, String name, @Nullable String code, Klass outputType, NodeRT previous,
                     ScopeRT scope, Value condition) {
        super(tmpId, name, code, outputType, previous, scope, condition);
    }

    @Override
    protected WhileNodeParam getParam(SerializeContext serializeContext) {
        return new WhileNodeParam(
                getCondition().toDTO(),
                getBodyScope().toDTO(true, serializeContext),
                NncUtils.map(getFields(), LoopField::toDTO)
        );
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitWhileNode(this);
    }
}
