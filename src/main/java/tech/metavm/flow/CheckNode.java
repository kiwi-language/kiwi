package tech.metavm.flow;

import tech.metavm.entity.*;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.expression.ExpressionUtil;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.flow.rest.CheckNodeParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.core.BooleanInstance;
import tech.metavm.util.InternalException;

@EntityType("检查节点")
public class CheckNode extends NodeRT<CheckNodeParamDTO> {

    public static CheckNode create(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope, IEntityContext context) {
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        CheckNodeParamDTO param = nodeDTO.getParam();
        var condition = ValueFactory.create(param.condition(), parsingContext);
        var exit = context.getEntity(BranchNode.class, param.exitRef());
        return new CheckNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope, condition, exit);
    }

    @ChildEntity("条件")
    private Value condition;

    @EntityField("退出位置")
    private BranchNode exit;

    public CheckNode(Long tmpId, String name, NodeRT<?> previous, ScopeRT scope, Value condition, BranchNode exit) {
        super(tmpId, name, null, previous, scope);
        this.condition = addChild(condition, "condition");
        this.exit = exit;
    }

    @Override
    protected CheckNodeParamDTO getParam(boolean persisting) {
        try(var context = SerializeContext.enter()) {
            return new CheckNodeParamDTO(condition.toDTO(persisting), context.getRef(exit));
        }
    }

    @Override
    protected void setParam(CheckNodeParamDTO param, IEntityContext context) {
        if (param.condition() != null) {
            condition = addChild(ValueFactory.create(param.condition(), getParsingContext(context)),
                    "condition");
        }
        if(param.exitRef() != null) {
            exit = context.getEntity(BranchNode.class, param.exitRef());
        }
    }

    public Value getCondition() {
        return condition;
    }

    @Override
    public void execute(MetaFrame frame) {
        var branch = getScope().getBranch();
        while (branch != null && branch.getOwner() != exit) {
            branch = branch.getOwner().getScope().getBranch();
        }
        if (branch == null) {
            throw new InternalException("Can not find an exit branch");
        }
        var checkResult = ((BooleanInstance) condition.evaluate(frame)).isTrue();
        if (!checkResult) {
            frame.setExitBranch(branch.getOwner(), branch);
            frame.jumpTo(branch.getOwner());
        }
    }

    public BranchNode getExit() {
        return exit;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitCheckNode(this);
    }
}
