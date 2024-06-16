package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.*;
import org.metavm.expression.FlowParsingContext;
import org.metavm.flow.rest.CheckNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.BooleanInstance;
import org.metavm.object.instance.core.Id;
import org.metavm.util.InternalException;

import javax.annotation.Nullable;

@EntityType
public class CheckNode extends NodeRT {

    public static CheckNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        CheckNodeParam param = nodeDTO.getParam();
        var condition = ValueFactory.create(param.condition(), parsingContext);
        var exit = context.getEntity(BranchNode.class, Id.parse(param.exitId()));
        CheckNode node = (CheckNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node != null) {
            node.setCondition(condition);
            node.setExit(exit);
        } else
            node = new CheckNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), prev, scope, condition, exit);
        return node;
    }

    private Value condition;

    private BranchNode exit;

    public CheckNode(Long tmpId, String name, @Nullable String code, NodeRT previous, ScopeRT scope, Value condition, BranchNode exit) {
        super(tmpId, name, code, null, previous, scope);
        this.condition = condition;
        this.exit = exit;
    }

    @Override
    protected CheckNodeParam getParam(SerializeContext serializeContext) {
        try (var serContext = SerializeContext.enter()) {
            return new CheckNodeParam(condition.toDTO(), serContext.getStringId(exit));
        }
    }

    private void setExit(BranchNode exit) {
        this.exit = exit;
    }

    private void setCondition(Value condition) {
        this.condition = condition;
    }

    public Value getCondition() {
        return condition;
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        var branch = getScope().getBranch();
        while (branch != null && branch.getOwner() != exit) {
            branch = branch.getOwner().getScope().getBranch();
        }
        if (branch == null)
            throw new InternalException("Can not find an exit branch");
        var checkResult = ((BooleanInstance) condition.evaluate(frame)).isTrue();
        if (checkResult) {
            return next();
        } else {
            frame.setExitBranch(branch.getOwner(), branch);
            return NodeExecResult.jump(branch.getOwner());
        }
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("if (!" + condition.getText() + ")");
        writer.indent();
        writer.writeNewLine("goto " + exit.getName());
        writer.unindent();
    }

    public BranchNode getExit() {
        return exit;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitCheckNode(this);
    }
}
