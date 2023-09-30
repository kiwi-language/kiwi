package tech.metavm.flow;

import tech.metavm.entity.*;
import tech.metavm.expression.ExpressionUtil;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.expression.ParsingContext;
import tech.metavm.flow.rest.BranchDTO;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.NncUtils;

import java.util.List;

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
                new ConstantValue(ExpressionUtil.trueExpression()),
                true,
                new ScopeRT(owner.getFlow(), owner),
                owner
        );
    }

    @EntityField("ID")
    private final long index;
    @EntityField("分支节点")
    private final BranchNode owner;
    @ChildEntity("范围")
    private final ScopeRT scope;
    @ChildEntity("条件")
    private Value condition;
    @EntityField("是否默认")
    private final boolean preselected;

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
        try(var context = SerializeContext.enter()) {
            return new BranchDTO(
                    getId(),
                    context.getTmpId(this),
                    index,
                    owner.getId(),
                    NncUtils.get(condition, v -> v.toDTO(persisting)),
                    scope.toDTO(withNodes),
                    preselected
            );
        }
    }

    private ParsingContext getParsingContext(IEntityContext entityContext) {
        return FlowParsingContext.create(owner.getScope(), owner, entityContext);
    }

    public boolean isPreselected() {
        return preselected;
    }

    public void update(BranchDTO branchDTO, IEntityContext entityContext) {
        if(branchDTO.condition() != null) {
            condition = ValueFactory.create(branchDTO.condition(), getParsingContext(entityContext));
        }
    }

    public List<Object> beforeRemove() {
        owner.deleteBranch(this);
        return List.of();
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
