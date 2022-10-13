package tech.metavm.flow;

import tech.metavm.flow.rest.BranchDTO;
import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.object.instance.query.FlowParsingContext;
import tech.metavm.object.instance.query.ParsingContext;
import tech.metavm.util.NncUtils;

import java.util.List;

public class Branch  {
    private final long id;
    private final BranchNode owner;
    private final ScopeRT scope;
    private Value condition;
    private final ParsingContext parsingContext;

    public Branch(long id, ValueDTO condition, ScopeRT scope, BranchNode owner) {
        this.id = id;
        this.owner = owner;
        this.scope = scope;
        parsingContext = FlowParsingContext.create(owner.getScope(), owner);
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
                scope.toDTO(withNodes)
        );
    }

    public void update(BranchDTO branchDTO) {
        condition = ValueFactory.getValue(branchDTO.condition(), parsingContext);
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
