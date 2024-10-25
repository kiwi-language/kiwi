package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.expression.ExpressionTypeMap;
import org.metavm.expression.Expressions;
import org.metavm.expression.FlowParsingContext;
import org.metavm.expression.TypeNarrower;
import org.metavm.flow.rest.IfNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.BooleanValue;

import javax.annotation.Nullable;
import java.util.Objects;

@EntityType
public class IfNode extends JumpNode {

    public static IfNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        var param = (IfNodeParam) nodeDTO.getParam();
        var condition = ValueFactory.create(param.condition(), parsingContext);
        var node = (IfNode) context.getNode(nodeDTO.id());
        if(node == null) {
            node = new IfNode(
                    nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(),
                    scope.getLastNode(), scope, condition, null
            );
        }
        if(stage == NodeSavingStage.FINALIZE)
            node.setTarget(Objects.requireNonNull(context.getNode(param.targetId())));
        return node;
    }

    private Value condition;

    private transient ExpressionTypeMap nextExpressionTypes;

    public IfNode(Long tmpId, @NotNull String name, @Nullable String code, @Nullable NodeRT previous, @NotNull ScopeRT scope,
                  Value condition, NodeRT target) {
        super(tmpId, name, code, null, previous, scope);
        this.condition = condition;
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
        return new IfNodeParam(condition.toDTO(), serializeContext.getStringId(getTarget()));
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        var b = ((BooleanValue) condition.evaluate(frame)).getValue();
        return b ? NodeExecResult.jump(getTarget()) : next();
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("if (" + condition.getText() + ") then goto " + getTarget().getName());
    }

    public void setCondition(Value condition) {
        this.condition = condition;
    }

    public Value getCondition() {
        return condition;
    }

    @Override
    public ExpressionTypeMap getNextExpressionTypes() {
        if(nextExpressionTypes == null) {
            var curExprTypes = getExpressionTypes();
            var narrower = new TypeNarrower(curExprTypes::getType);
            nextExpressionTypes = curExprTypes.merge(narrower.narrowType(Expressions.not(condition.getExpression())));
        }
        return nextExpressionTypes;
    }

}
