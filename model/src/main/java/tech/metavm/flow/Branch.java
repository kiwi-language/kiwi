package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.expression.Expressions;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.expression.ParsingContext;
import tech.metavm.flow.rest.BranchDTO;
import tech.metavm.util.Instances;
import tech.metavm.util.NncUtils;

@EntityType
public class Branch extends Element implements LocalKey {

    private static final long PRESELECTED_BRANCH_ID = 10000;

    public static Branch create(long index, BranchNode owner) {
        return new Branch(
                index,
                Values.constant(Expressions.constant(Instances.trueInstance())),
                false,
                false,
                owner
        );
    }

    public static Branch createPreselected(BranchNode owner, boolean isExit) {
        return new Branch(
                PRESELECTED_BRANCH_ID,
                Values.constant(Expressions.trueExpression()),
                true,
                isExit,
                owner
        );
    }

    private final long index;
    private final BranchNode owner;
    @ChildEntity
    private final ScopeRT scope;
    private Value condition;
    private final boolean preselected;
    private final boolean isExit;

    public Branch(long index, Value condition, boolean preselected, boolean isExit, BranchNode owner) {
        this.index = index;
        this.owner = owner;
        this.scope = addChild(new ScopeRT(owner.getFlow(), owner), "scope");
        this.preselected = preselected;
        this.condition = condition;
        if (isExit) {
            NncUtils.requireTrue(preselected, "Only default branch can be an exit");
        }
        this.isExit = isExit;
        scope.setBranch(this);
    }

    public long getIndex() {
        return index;
    }

    public Value getCondition() {
        return condition;
    }

    public BranchNode getOwner() {
        return owner;
    }

    public ScopeRT getScope() {
        return scope;
    }

    public boolean isExit() {
        return isExit;
    }

    public BranchDTO toDTO(boolean withNodes, SerializeContext serContext) {
        return new BranchDTO(
                serContext.getStringId(this),
                index,
                owner.getStringId(),
                NncUtils.get(condition, Value::toDTO),
                scope.toDTO(withNodes, serContext),
                preselected,
                isExit
        );
    }

    private ParsingContext getParsingContext(IEntityContext entityContext) {
        return FlowParsingContext.create(owner.getScope(), owner, entityContext);
    }

    public boolean isPreselected() {
        return preselected;
    }

    public void update(BranchDTO branchDTO, IEntityContext entityContext) {
        if (branchDTO.condition() != null) {
            setCondition(ValueFactory.create(branchDTO.condition(), getParsingContext(entityContext)));
        }
    }

    public void setCondition(Value condition) {
        this.condition = condition;
    }

    public boolean checkCondition(MetaFrame frame) {
        return Instances.isTrue(condition.evaluate(frame));
    }

    public boolean isEmpty() {
        return scope.isEmpty();
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitBranch(this);
    }

    @Override
    public boolean isValidLocalKey() {
        return true;
    }

    @Override
    public String getLocalKey(@NotNull BuildKeyContext context) {
        return Long.toString(index);
    }

    public void writeCode(CodeWriter writer) {
        writer.writeNewLine("case " + condition.getText());
        writer.writeNewLine(" -> ");
        scope.writeCode(writer);
    }

    public boolean isTerminating() {
        var lastNode = scope.getLastNode();
        return lastNode != null && lastNode.isExit();
    }

}
