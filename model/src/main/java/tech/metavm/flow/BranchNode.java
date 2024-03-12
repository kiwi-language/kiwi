package tech.metavm.flow;

import tech.metavm.common.ErrorCode;
import tech.metavm.common.RefDTO;
import tech.metavm.entity.*;
import tech.metavm.flow.rest.BranchDTO;
import tech.metavm.flow.rest.BranchNodeParam;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.type.ClassType;
import tech.metavm.util.BusinessException;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@EntityType("分支节点")
public class BranchNode extends NodeRT {

    public static BranchNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
        BranchNodeParam param = nodeDTO.getParam();
        BranchNode node;
        if (nodeDTO.id() != null) {
            node = (BranchNode) context.getNode(nodeDTO.id());
        } else
            node = new BranchNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), param.inclusive(), prev, scope);
        node.setInclusive(param.inclusive());
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
            var parsingContext = node.getParsingContext(context);
            List<Branch> branches = new ArrayList<>();
            for (int i = 0; i < param.branches().size(); i++) {
                BranchDTO branchDTO = param.branches().get(i);
                var branch = context.getEntity(Branch.class, branchDTO.getRef());
                if (branch == null) {
                    branch = new Branch(
                            branchDTO.index(),
                            ValueFactory.create(branchDTO.condition(), parsingContext),
                            branchDTO.preselected(),
                            branchDTO.isExit(),
                            node
                    );
                    branch.setTmpId(branchDTO.tmpId());
                    branches.add(i, branch);
                    context.bind(branch);
                } else {
                    if (branch.getOwner() != node) {
                        throw new BusinessException(ErrorCode.BRANCH_OWNER_MISMATCH,
                                branch.getOwner().getName() + "/" + branch.getIndex(),
                                node.getName());
                    }
                    branch.update(branchDTO, context);
                    branches.add(branch);
                }
            }
            node.setBranches(branches);
        }
        return node;
    }

    public void setBranches(List<Branch> branches) {
        this.branches.resetChildren(branches);
    }

    public void setInclusive(boolean inclusive) {
        this.inclusive = inclusive;
    }

    @EntityField("是否包含")
    private boolean inclusive;
    @ChildEntity("分支列表")
    private final ChildArray<Branch> branches = addChild(new ChildArray<>(Branch.class), "branches");

    public BranchNode(Long tmpId, String name, @Nullable String code, boolean inclusive, NodeRT prev, ScopeRT scope) {
        super(tmpId, name, code, null, prev, scope);
        this.inclusive = inclusive;
    }

    @Override
    protected BranchNodeParam getParam(SerializeContext serializeContext) {
        return new BranchNodeParam(
                inclusive,
                NncUtils.map(branches, branch -> branch.toDTO(true, serializeContext))
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
        Branch branch = new Branch(branchId, condition, false, false, this);
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
        return branches.get(Entity::tryGetId, id);
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
    public NodeExecResult execute(MetaFrame frame) {
        var exitBranch = frame.removeExitBranch(this);
        for (Branch branch : branches) {
            if (exitBranch != null) {
                if (branch == exitBranch) {
                    exitBranch = null;
                }
                continue;
            }
            if (branch.checkCondition(frame)) {
                frame.setSelectedBranch(this, branch);
                if (branch.isNotEmpty())
                    return NodeExecResult.jump(branch.getScope().tryGetFirstNode());
                else
                    return next();
            }
        }
        throw new InternalException("No matching branch");
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("switch {");
        writer.indent();
        for (Branch branch : branches) {
            branch.writeCode(writer);
        }
        writer.unindent();
        writer.writeNewLine("}");
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
                "Default branch is missing in branch node " + getName() + "(" + tryGetId() + ")");
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitBranchNode(this);
    }
}
