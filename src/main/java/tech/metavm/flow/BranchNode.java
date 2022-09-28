package tech.metavm.flow;

import tech.metavm.flow.persistence.NodePO;
import tech.metavm.flow.rest.BranchDTO;
import tech.metavm.flow.rest.BranchParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

public class BranchNode extends NodeRT<BranchParamDTO> {

    private final List<Branch> branches = new ArrayList<>();

    public BranchNode(NodeDTO nodeDTO, BranchParamDTO param, ScopeRT scope) {
        super(nodeDTO, null, scope);
        for (BranchDTO branchDTO : param.branches()) {
            addBranch(branchDTO);
        }
    }

    public BranchNode(NodePO nodePO, BranchParamDTO param, ScopeRT scope) {
        super(nodePO, scope);
        for (BranchDTO branchDTO : param.branches()) {
            branches.add(
                new Branch(
                    branchDTO.id(),
                    branchDTO.condition(),
                    getFromContext(ScopeRT.class, branchDTO.scope().id()),
                    this
                )
            );
        }
    }

    @Override
    protected void setParam(BranchParamDTO param) {

    }

    @Override
    protected BranchParamDTO getParam(boolean forPersistence) {
        return new BranchParamDTO(NncUtils.map(branches, branch -> branch.toDTO(!forPersistence)));
    }

    public Branch addBranch(BranchDTO branchDTO) {
        long branchId;
        if(branches.isEmpty()) {
            branchId = 1;
        }
        else {
            long maxId = 1;
            for (Branch branch : branches) {
                maxId = Math.max(branch.getId(), maxId);
            }
            branchId = maxId + 1;
        }
        Branch branch = new Branch(branchId, branchDTO.condition(), new ScopeRT(getFlow()), this);
        branches.add(branch);
        return branch;
    }

    public List<Branch> getBranches() {
        return Collections.unmodifiableList(branches);
    }

    public Branch getBranch(long id) {
        return NncUtils.filterOne(branches, branch -> branch.getId() == id);
    }

    public Branch deleteBranch(long id) {
        ListIterator<Branch> listIt = branches.listIterator();
        while (listIt.hasNext()) {
            Branch branch = listIt.next();
            if(branch.getId() == id) {
                listIt.remove();
                return branch;
            }
        }
        return null;
    }

    @Override
    public void execute(FlowFrame frame) {
    }

}
