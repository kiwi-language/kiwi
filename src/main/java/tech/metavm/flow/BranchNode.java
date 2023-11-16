package tech.metavm.flow;

import tech.metavm.common.ErrorCode;
import tech.metavm.common.RefDTO;
import tech.metavm.entity.*;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.flow.rest.BranchDTO;
import tech.metavm.flow.rest.BranchParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.type.ClassType;
import tech.metavm.util.*;

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
    private final ChildArray<Branch> branches = addChild(new ChildArray<>(Branch.class), "branches");

    public BranchNode(Long tmpId, String name, boolean inclusive, NodeRT<?> prev, ScopeRT scope) {
        super(tmpId, name, null, prev, scope);
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
            List<Branch> branches = new ArrayList<>();
            for (int i = 0; i < param.branches().size(); i++) {
                BranchDTO branchDTO = param.branches().get(i);
                var branch = entityContext.getEntity(Branch.class, branchDTO.getRef());
                if (branch == null) {
                    branch = new Branch(
                            branchDTO.index(),
                            ValueFactory.create(branchDTO.condition(), getParsingContext(entityContext)),
                            branchDTO.preselected(),
                            branchDTO.isExit(),
                            new ScopeRT(getFlow(), this),
                            this
                    );
                    branch.setTmpId(branchDTO.tmpId());
                    branches.add(i, branch);
                    entityContext.bind(branch);
                } else {
                    if (branch.getOwner() != this) {
                        throw new BusinessException(ErrorCode.BRANCH_OWNER_MISMATCH,
                                branch.getOwner().getName() + "/" + branch.getIndex(),
                                getName());
                    }
                    branch.update(branchDTO, entityContext);
                    branches.add(branch);
                }
            }
            this.branches.resetChildren(branches);
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

    public void addBranch(Branch branch) {
        NncUtils.requireTrue(branch.getOwner() == this);
        this.branches.addChild(branch);
    }

    public Branch addBranch(Value condition) {
        long branchId;
        long maxIndex = 1;
        for (Branch branch : branches) {
            if (!branch.isPreselected()) {
                maxIndex = Math.max(branch.getIndex(), maxIndex);
            }
        }
        branchId = maxIndex + 1;
        Branch branch = new Branch(branchId, condition, false, false, new ScopeRT(getFlow(), this), this);
        if (!branches.isEmpty() && branches.get(branches.size() - 1).isPreselected()) {
            branches.addChild(branches.size() - 1, branch);
        } else {
            branches.addChild(branch);
        }
        return branch;
    }

    public Branch addDefaultBranch() {
        return addDefaultBranch(false);
    }

    public Branch addDefaultBranch(boolean isExit) {
        var branch = Branch.createPreselected(this, isExit);
        branches.addChild(branch);
        return branch;
    }

    public ReadonlyArray<Branch> getBranches() {
        return branches;
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
    public void execute(MetaFrame frame) {
        var exitBranch = frame.getExitBranch(this);
        for (Branch branch : branches) {
            if (exitBranch != null) {
                if (branch == exitBranch) {
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
        if (getSuccessor() instanceof MergeNode mergeNode) {
            return List.of(mergeNode);
        } else {
            return List.of();
        }
    }

    public boolean isInclusive() {
        return inclusive;
    }

    public Branch getDefaultBranch() {
        return NncUtils.requireNonNull(branches.get(Branch::isPreselected, true),
                "Default branch is missing in branch node " + getName() + "(" + getId() + ")");
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitBranchNode(this);
    }
}
