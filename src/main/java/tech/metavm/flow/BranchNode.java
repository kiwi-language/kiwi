package tech.metavm.flow;

import tech.metavm.dto.ErrorCode;
import tech.metavm.dto.RefDTO;
import tech.metavm.entity.*;
import tech.metavm.flow.rest.BranchDTO;
import tech.metavm.flow.rest.BranchParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.meta.ClassType;
import tech.metavm.util.BusinessException;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;

import java.util.*;

@EntityType("分支节点")
public class BranchNode extends NodeRT<BranchParamDTO> {

    public static BranchNode create(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope, IEntityContext context) {
        BranchParamDTO param = nodeDTO.getParam();
        BranchNode node = new BranchNode(nodeDTO.tmpId(), nodeDTO.name(), param.inclusive(), prev, scope);
        node.setParam(nodeDTO.getParam(), context);
        return node;
    }

    @EntityField("是否包容")
    private boolean inclusive;
    @ChildEntity("分支列表")
    private final Table<Branch> branches = new Table<>(Branch.class, true);

    public BranchNode(Long tmpId, String name, boolean inclusive, NodeRT<?> prev, ScopeRT scope) {
        super(tmpId, name,  null, prev, scope);
        this.inclusive = inclusive;
    }

    @Override
    protected void setParam(BranchParamDTO param, IEntityContext entityContext) {
        inclusive = param.inclusive();
        if (param.branches() != null) {
            Set<RefDTO> branchRefs = NncUtils.mapAndFilterUnique(param.branches(), BranchDTO::getRef, Objects::nonNull);
            if (branchRefs.size() > param.branches().size()) {
                throw new BusinessException(ErrorCode.BRANCH_INDEX_DUPLICATE);
            }
            if (branchRefs.size() < param.branches().size()) {
                throw new BusinessException(ErrorCode.BRANCH_INDEX_REQUIRED);
            }
            if (NncUtils.count(param.branches(), BranchDTO::preselected) != 1) {
                throw new BusinessException(ErrorCode.NUM_PRESELECTED_BRANCH_NOT_EQUAL_TO_ONE);
            }
            branches.removeIf(branch -> !branchRefs.contains(branch.getRef()));
            for (BranchDTO branchDTO : param.branches()) {
                var branch = entityContext.getEntity(Branch.class, branchDTO.getRef());
                if (branch == null) {
                    branch = new Branch(
                            branchDTO.index(),
                            ValueFactory.create(branchDTO.condition(), getParsingContext(entityContext)),
                            branchDTO.preselected(),
                            new ScopeRT(getFlow(), this),
                            this
                    );
                    branch.setTmpId(branchDTO.tmpId());
                    branches.add(branch);
                    entityContext.bind(branch);
                } else {
                    if(branch.getOwner() != this) {
                        throw new BusinessException(ErrorCode.BRANCH_OWNER_MISMATCH,
                                branch.getOwner().getName() + "/" + branch.getIndex(),
                                getName());
                    }
                    branch.update(branchDTO, entityContext);
                }
            }
        }
    }

    @Override
    protected BranchParamDTO getParam(boolean persisting) {
        return new BranchParamDTO(
                inclusive,
                NncUtils.map(branches, branch -> branch.toDTO(!persisting, persisting))
//                NncUtils.map(fields, f -> f.toDTO(persisting))
        );
    }

    public Branch addBranch(BranchDTO branchDTO, IEntityContext entityContext) {
        return addBranch(
                ValueFactory.create(branchDTO.condition(), getParsingContext(entityContext))
        );
    }

//    public BranchNodeOutputField getOutputField(Field field) {
//        return fields.get(BranchNodeOutputField::getField, field);
//    }
//
//    public void setOutput(Field field, BranchNodeOutputField outputField) {
//        fields.remove(BranchNodeOutputField::getField, field);
//        fields.add(outputField);
//    }

    public Branch addBranch(Value condition) {
        long branchId;
        long maxIndex = 1;
        for (Branch branch : branches) {
            if (!branch.isPreselected()) {
                maxIndex = Math.max(branch.getIndex(), maxIndex);
            }
        }
        branchId = maxIndex + 1;
        Branch branch = new Branch(branchId, condition, false, new ScopeRT(getFlow(), this), this);
        if (!branches.isEmpty() && branches.get(branches.size() - 1).isPreselected()) {
            branches.add(branches.size() - 1, branch);
        } else {
            branches.add(branch);
        }
        return branch;
    }

    public Branch addDefaultBranch() {
        var branch = Branch.createPreselected(this);
        branches.add(branch);
        return branch;
    }

    public List<Branch> getBranches() {
        return new ArrayList<>(branches);
    }

    public Branch getBranchById(long id) {
        return branches.get(Entity::getId, id);
    }

    public Branch getBranchByIndex(int index) {
        return branches.get(index);
    }

    public Branch getBranchByIndex(long index) {
        return branches.get(Branch::getIndex, index);
    }

    public void deleteBranch(Branch branch) {
        if (!branches.remove(branch)) {
            throw new InternalException(branch + " does not exist in " + this);
        }
    }

    @Override
    public ClassType getType() {
        return (ClassType) super.getType();
    }

    @Override
    public void execute(FlowFrame frame) {
        var exitBranch = frame.getExitBranch(this);
        for (Branch branch : branches) {
            if(exitBranch != null) {
                if(branch == exitBranch) {
                    exitBranch = null;
                }
                continue;
            }
            if (branch.checkCondition(frame)) {
                if (branch.isNotEmpty()) {
                    frame.jumpTo(branch.getScope().getFirstNode());
                }
                frame.setSelectedBranch(this, branch);
                break;
            }
        }
    }

    @Override
    protected List<Object> nodeBeforeRemove() {
        if(getSuccessor() instanceof MergeNode mergeNode) {
            return List.of(mergeNode);
        }
        else {
            return List.of();
        }
    }

    public boolean isInclusive() {
        return inclusive;
    }
}
