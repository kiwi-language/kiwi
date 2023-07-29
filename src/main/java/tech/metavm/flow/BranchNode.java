package tech.metavm.flow;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.flow.rest.BranchDTO;
import tech.metavm.flow.rest.BranchParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

@EntityType("分支节点")
public class BranchNode extends NodeRT<BranchParamDTO> {

    @EntityField("是否包容")
    private boolean inclusive;
    @ChildEntity("分支列表")
    private final Table<Branch> branches = new Table<>(Branch.class, true);

    public BranchNode(NodeDTO nodeDTO, ScopeRT scope) {
        super(nodeDTO, null, scope);
        BranchParamDTO param = nodeDTO.getParam();
        inclusive = param.inclusive();
        branches.add(Branch.create(1L, this));
        branches.add(Branch.createPreselected(this));
    }

    @Override
    protected void setParam(BranchParamDTO param, IEntityContext entityContext) {
        inclusive = param.inclusive();
    }

    @Override
    protected BranchParamDTO getParam(boolean persisting) {
        return new BranchParamDTO(
                inclusive,
                NncUtils.map(branches, branch -> branch.toDTO(!persisting, persisting))
        );
    }

    public Branch addBranch(BranchDTO branchDTO, IEntityContext entityContext) {
        return addBranch(
                ValueFactory.getValue(branchDTO.condition(), getParsingContext(entityContext))
        );
    }

    public Branch addBranch(Value condition) {
        long branchId;
        long maxIndex = 1;
        for (Branch branch : branches) {
            if(!branch.isPreselected()) {
                maxIndex = Math.max(branch.getIndex(), maxIndex);
            }
        }
        branchId = maxIndex + 1;
        Branch branch = new Branch(branchId, condition, false, new ScopeRT(getFlow(), this), this);
        branches.add(branches.size() - 1, branch);
        return branch;
    }

    public List<Branch> getBranches() {
        return new ArrayList<>(branches);
    }

    public Branch getBranchByIndex(int index) {
        return branches.get(index);
    }

    public Branch getBranch(long index) {
        return NncUtils.find(branches, branch -> branch.getIndex() == index);
    }

    public void deleteBranch(Branch branch) {
        if(!branches.remove(branch)) {
            throw new InternalException(branch + " does not exist in " + this);
        }
    }

    @Override
    public void execute(FlowFrame frame) {
        for (Branch branch : branches) {
            if(branch.checkCondition(frame)) {
                if(branch.isNotEmpty()) {
                    frame.jumpTo(branch.getScope().getFirstNode());
                }
                break;
            }
        }
    }

}
