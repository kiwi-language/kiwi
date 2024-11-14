package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.expression.ExpressionTypeMap;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.flow.rest.IfNodeParam;
import org.metavm.flow.rest.NodeDTO;

import javax.annotation.Nullable;
import java.util.Objects;

@EntityType
public class IfNode extends JumpNode {

    public static IfNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        var node = (IfNode) context.getNode(nodeDTO.id());
        if(node == null) {
            node = new IfNode(
                    nodeDTO.tmpId(), nodeDTO.name(),
                    scope.getLastNode(), scope, null
            );
        }
        if(stage == NodeSavingStage.FINALIZE) {
            var param = (IfNodeParam) nodeDTO.getParam();
            node.setTarget(Objects.requireNonNull(context.getNode(param.targetId())));
        }
        return node;
    }

    private transient ExpressionTypeMap nextExpressionTypes;

    public IfNode(Long tmpId, @NotNull String name, @Nullable NodeRT previous, @NotNull ScopeRT scope,
                  NodeRT target) {
        super(tmpId, name, null, previous, scope);
//        var narrower = new TypeNarrower(getExpressionTypes()::getType);
//        mergeExpressionTypes(narrower.narrowType(Expressions.not(condition.getExpression())));
        if(target != null)
            setTarget(target);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitIfNode(this);
    }

    @Override
    protected IfNodeParam getParam(SerializeContext serializeContext) {
        return new IfNodeParam(serializeContext.getStringId(getTarget()));
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("if " + getTarget().getName());
    }

    @Override
    public ExpressionTypeMap getNextExpressionTypes() {
//        if(nextExpressionTypes == null) {
//            var curExprTypes = getExpressionTypes();
//            var narrower = new TypeNarrower(curExprTypes::getType);
//            nextExpressionTypes = curExprTypes.merge(narrower.narrowType(Expressions.not(condition.getExpression())));
//        }
        return nextExpressionTypes;
    }

    @Override
    public int getStackChange() {
        return -1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.IF);
        output.writeShort(getTarget().getOffset());
    }

    @Override
    public int getLength() {
        return 3;
    }

}
