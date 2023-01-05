package tech.metavm.flow;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.flow.rest.BranchDTO;
import tech.metavm.object.instance.query.ExpressionUtil;
import tech.metavm.object.instance.query.FlowParsingContext;
import tech.metavm.object.instance.query.ParsingContext;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.NncUtils;

@EntityType("分支")
public class Branch extends Entity {

    private static final long PRESELECTED_BRANCH_ID = 10000;

    public static Branch create(long index, BranchNode owner) {
        return new Branch(
                index,
                new ConstantValue(ExpressionUtil.constant(InstanceUtils.trueInstance())),
                false,
                new ScopeRT(owner.getFlow(), owner),
                owner
        );
    }

    public static Branch createPreselected(BranchNode owner) {
        return new Branch(
                PRESELECTED_BRANCH_ID,
                new ConstantValue(ExpressionUtil.constant(InstanceUtils.trueInstance())),
                true,
                new ScopeRT(owner.getFlow(), owner),
                owner
        );
    }

    @EntityField("ID")
    private final long index;
    @EntityField("分支节点")
    private final BranchNode owner;
    @EntityField("范围")
    private final ScopeRT scope;
    @EntityField("条件")
    private Value condition;
    @EntityField("是否默认")
    private final boolean preselected;

    private transient ParsingContext parsingContext;

    public Branch(long index, Value condition, boolean preselected, ScopeRT scope, BranchNode owner) {
        this.index = index;
        this.owner = owner;
        this.scope = scope;
        this.preselected = preselected;
        this.condition = condition;
    }

    public long getIndex() {
        return index;
    }

    public Value getCondition() {
        return condition;
    }

    public NodeRT<?> getOwner() {
        return owner;
    }

    public ScopeRT getScope() {
        return scope;
    }

    public BranchDTO toDTO(boolean withNodes, boolean persisting) {
        return new BranchDTO(
                index,
                owner.getId(),
                NncUtils.get(condition, v -> v.toDTO(persisting)),
                scope.toDTO(withNodes),
                preselected
        );
    }

    private ParsingContext getParsingContext(IEntityContext entityContext) {
        return FlowParsingContext.create(owner.getScope(), owner, entityContext);
    }

    public boolean isPreselected() {
        return preselected;
    }

    public void update(BranchDTO branchDTO, IEntityContext entityContext) {
        condition = ValueFactory.getValue(branchDTO.condition(), getParsingContext(entityContext));
    }

    public void remove() {
        owner.deleteBranch(this.index);
        getScope().remove();
    }

    public boolean checkCondition(FlowFrame frame) {
        return InstanceUtils.isTrue(condition.evaluate(frame));
    }

    public boolean isEmpty() {
        return scope.isEmpty();
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }

}
