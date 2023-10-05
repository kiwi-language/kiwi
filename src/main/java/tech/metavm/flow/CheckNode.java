package tech.metavm.flow;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.flow.rest.CheckNodeParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.BooleanInstance;
import tech.metavm.util.InternalException;

@EntityType("检查节点")
public class CheckNode extends NodeRT<CheckNodeParamDTO> {

    public static CheckNode create(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope, IEntityContext context) {
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        CheckNodeParamDTO param = nodeDTO.getParam();
        var condition = ValueFactory.create(param.condition(), parsingContext);
        return new CheckNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope, condition);
    }

    @ChildEntity("条件")
    private Value condition;

    public CheckNode(Long tmpId, String name, NodeRT<?> previous, ScopeRT scope, Value condition) {
        super(tmpId, name, null, previous, scope);
        this.condition = condition;
    }

    @Override
    protected CheckNodeParamDTO getParam(boolean persisting) {
        return new CheckNodeParamDTO(condition.toDTO(persisting));
    }

    @Override
    protected void setParam(CheckNodeParamDTO param, IEntityContext context) {
        if(param.condition() != null) {
            condition = ValueFactory.create(param.condition(), getParsingContext(context));
        }
    }

    public Value getCondition() {
        return condition;
    }

    @Override
    public boolean isExit() {
        return true;
    }

    @Override
    public void execute(FlowFrame frame) {
        var branch = getScope().getBranch();
        while (branch != null && branch.isPreselected()) {
            var owner = branch.getScope().getOwner();
            branch = owner != null ? owner.getScope().getBranch() : null;
        }
        if (branch == null) {
            throw new InternalException("Exit branch failed. No other branches to enter.");
        }
        var checkResult = ((BooleanInstance)condition.evaluate(frame)).isTrue();
        if(!checkResult) {
            frame.setExitBranch(branch.getOwner(), branch);
            frame.jumpTo(branch.getOwner());
        }
    }
}
