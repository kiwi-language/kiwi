package tech.metavm.flow;

import tech.metavm.flow.rest.BranchDTO;
import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.util.NncUtils;

import java.util.List;

public class Branch  {
    private final long id;
    private final BranchNode owner;
    private final ScopeRT scope;
    private Value condition;

    public Branch(long id, ValueDTO condition, ScopeRT scope, BranchNode owner) {
        this.id = id;
        this.owner = owner;
        this.scope = scope;
        this.condition = ValueFactory.getValue(condition);
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

    public BranchDTO toDTO(boolean withNodes) {
        return new BranchDTO(
                id,
                owner.getId(),
                NncUtils.get(condition, Value::toDTO),
                scope.toDTO(withNodes)
        );
    }

    public void update(BranchDTO branchDTO) {
        condition = ValueFactory.getValue(branchDTO.condition());
    }
}
