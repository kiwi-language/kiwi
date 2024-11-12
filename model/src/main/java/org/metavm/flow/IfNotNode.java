package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.expression.ExpressionTypeMap;
import org.metavm.flow.rest.IfNotNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.BooleanValue;

import javax.annotation.Nullable;
import java.util.Objects;

@EntityType
public class IfNotNode extends JumpNode {

    public static IfNotNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        var node = (IfNotNode) context.getNode(nodeDTO.id());
        if(node == null) {
            node = new IfNotNode(
                    nodeDTO.tmpId(), nodeDTO.name(),
                    scope.getLastNode(), scope, null
            );
        }
        if(stage == NodeSavingStage.FINALIZE) {
            var param = (IfNotNodeParam) nodeDTO.getParam();
            node.setTarget(Objects.requireNonNull(context.getNode(param.targetId())));
        }
        return node;
    }

    private transient ExpressionTypeMap nextExpressionTypes;

    public IfNotNode(Long tmpId, @NotNull String name, @Nullable NodeRT previous, @NotNull ScopeRT scope,
                     NodeRT target) {
        super(tmpId, name, null, previous, scope);
        if(target != null)
            setTarget(target);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitIfNotNode(this);
    }

    @Override
    protected IfNotNodeParam getParam(SerializeContext serializeContext) {
        return new IfNotNodeParam(serializeContext.getStringId(getTarget()));
    }

    @Override
    public int execute(MetaFrame frame) {
        if(!((BooleanValue) frame.pop()).getValue()) {
            frame.setJumpTarget(getTarget());
            return MetaFrame.STATE_JUMP;
        }
        else
            return MetaFrame.STATE_NEXT;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("if not " + getTarget().getName());
    }

    @Override
    public ExpressionTypeMap getNextExpressionTypes() {
//        if(nextExpressionTypes == null) {
//            var curExprTypes = getExpressionTypes();
//            var narrower = new TypeNarrower(curExprTypes::getType);
//            nextExpressionTypes = curExprTypes.merge(narrower.narrowType(condition.getExpression()));
//        }
        return nextExpressionTypes;
    }

    @Override
    public int getStackChange() {
        return -1;
    }

}
