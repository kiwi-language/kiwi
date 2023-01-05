package tech.metavm.flow;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.flow.rest.BranchDTO;
import tech.metavm.flow.rest.BranchParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

@EntityType("分支节点")
public class BranchNode extends NodeRT<BranchParamDTO> {

    @EntityField("是否包容")
    private boolean inclusive;
    @EntityField("分支列表")
    private final Table<Branch> branches = new Table<>(Branch.class);

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

    public Branch getBranch(long index) {
        return NncUtils.find(branches, branch -> branch.getIndex() == index);
    }

    public Branch deleteBranch(long index) {
        ListIterator<Branch> listIt = branches.listIterator();
        while (listIt.hasNext()) {
            Branch branch = listIt.next();
            if(branch.getIndex() == index) {
                listIt.remove();
                return branch;
            }
        }
        return null;
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
