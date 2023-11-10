package tech.metavm.flow;

import org.jetbrains.annotations.Nullable;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.expression.ExpressionUtil;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.expression.ParsingContext;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.flow.rest.WhileParamDTO;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;

@EntityType("While节点")
public class WhileNode extends LoopNode<WhileParamDTO> {

    public static WhileNode create(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope, IEntityContext context) {
        var outputType = context.getClassType(nodeDTO.outputTypeRef());
        Value condition = new ExpressionValue(ExpressionUtil.trueExpression());
        // IMPORTANT COMMENT DON"T REMOVE:
        // DO NOT call setParam here. setParam should be called after the loop body has been constructed.
        // See FlowManager.saveLoopNodeContent
        var node = new WhileNode(nodeDTO.tmpId(), nodeDTO.name(), outputType, prev, scope, condition);
        ParsingContext parsingContext = FlowParsingContext.create(scope, node, context);
        WhileParamDTO param = nodeDTO.getParam();
        condition = ValueFactory.create(param.getCondition(), parsingContext);
        node.setCondition(condition);
        return node;
    }

    public WhileNode(Long tmpId, String name, @Nullable ClassType outputType, NodeRT<?> previous,
                     ScopeRT scope, Value condition) {
        super(tmpId, name, outputType, previous, scope, condition);
    }

    @Override
    protected WhileParamDTO getParam(boolean persisting) {
        return new WhileParamDTO(
                getCondition().toDTO(persisting),
                getBodyScope().toDTO(!persisting),
                NncUtils.map(getFields(), field -> field.toDTO(persisting))
        );
    }

    @Override
    protected void setParam(WhileParamDTO param, IEntityContext context) {
        setLoopParam(param, context);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitWhileNode(this);
    }
}
