package tech.metavm.flow;

import tech.metavm.entity.EntityField;
import tech.metavm.flow.rest.BranchDTO;
import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.object.instance.query.FlowParsingContext;
import tech.metavm.object.instance.query.ParsingContext;
import tech.metavm.util.NncUtils;

@tech.metavm.entity.ValueType("分支")
public class Branch {

    private static final long PRESELECTED_BRANCH_ID = 10000;

    public static Branch create(long id, BranchNode owner) {
        return new Branch(
                id, null, false, new ScopeRT(owner.getFlow()), owner
        );
    }

    public static Branch createPreselected(BranchNode owner) {
        return new Branch(
                PRESELECTED_BRANCH_ID,
                new ValueDTO(ValueKind.CONSTANT.code(), true, null),
                true,
                new ScopeRT(owner.getFlow()),
                owner
        );
    }

    @EntityField("ID")
    private final long id;
    @EntityField("分支节点")
    private final BranchNode owner;
    @EntityField("范围")
    private final ScopeRT scope;
    @EntityField("条件")
    private Value condition;
    @EntityField("是否默认")
    private final boolean preselected;

    private transient ParsingContext parsingContext;

    public Branch(long id, ValueDTO condition, boolean preselected, ScopeRT scope, BranchNode owner) {
        this.id = id;
        this.owner = owner;
        this.scope = scope;
        this.preselected = preselected;
        parsingContext = getParsingContext();
        this.condition = ValueFactory.getValue(condition, parsingContext);
    }

    public long getId() {
        return id;
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
                id,
                owner.getId(),
                NncUtils.get(condition, v -> v.toDTO(persisting)),
                scope.toDTO(withNodes),
                preselected
        );
    }

    private ParsingContext getParsingContext() {
        if(parsingContext == null) {
            parsingContext = FlowParsingContext.create(owner.getScope(), owner);
        }
        return parsingContext;
    }

    public boolean isPreselected() {
        return preselected;
    }

    public void update(BranchDTO branchDTO) {
        condition = ValueFactory.getValue(branchDTO.condition(), getParsingContext());
    }

    public void remove() {
        owner.deleteBranch(this.id);
        getScope().remove();
    }

    public boolean checkCondition(FlowFrame frame) {
        return Boolean.TRUE.equals(condition.evaluate(frame));
    }

    public boolean isEmpty() {
        return scope.isEmpty();
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }

}
