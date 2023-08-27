package tech.metavm.flow;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.flow.rest.BranchDTO;
import tech.metavm.flow.rest.BranchParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EntityType("分支节点")
public class BranchNode extends NodeRT<BranchParamDTO> {

    public static BranchNode create(NodeDTO nodeDTO, ScopeRT scope, IEntityContext context) {
        var outputType = context.getClassType(nodeDTO.outputTypeId());
        return new BranchNode(nodeDTO, outputType, scope);
    }

    @EntityField("是否包容")
    private boolean inclusive;
    @ChildEntity("分支列表")
    private final Table<Branch> branches = new Table<>(Branch.class, true);
    @ChildEntity("字段值")
    private final Table<BranchNodeOutputField> fields = new Table<>(BranchNodeOutputField.class);

    public BranchNode(NodeDTO nodeDTO, ClassType outputType, ScopeRT scope) {
        super(nodeDTO, outputType, scope);
        BranchParamDTO param = nodeDTO.getParam();
        inclusive = param.inclusive();
        if (param.branches() == null) {
            branches.add(Branch.create(1L, this));
            branches.add(Branch.createPreselected(this));
        }
    }

    public BranchNode(String name, boolean inclusive, NodeRT<?> prev, ScopeRT scope) {
        super(name, NodeKind.BRANCH, new ClassType(name + "_output"), prev, scope);
        this.inclusive = inclusive;
    }

    @Override
    protected void setParam(BranchParamDTO param, IEntityContext entityContext) {
        inclusive = param.inclusive();
        if (param.branches() != null) {
            Map<Long, Branch> index2branch = new HashMap<>();
            for (Branch branch : branches) {
                index2branch.put(branch.getIndex(), branch);
            }
            branches.clear();
            for (BranchDTO branchDTO : param.branches()) {
                var existing = index2branch.get(branchDTO.id());
                if (existing != null) {
                    existing.update(branchDTO, entityContext);
                    branches.add(existing);
                } else {
                    addBranch(branchDTO, entityContext);
                }
            }
        }
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

    public BranchNodeOutputField getOutputField(Field field) {
        return fields.get(BranchNodeOutputField::getField, field);
    }

    public void setOutput(Field field, BranchNodeOutputField outputField) {
        fields.remove(BranchNodeOutputField::getField, field);
        fields.add(outputField);
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

    public Branch getBranchByIndex(int index) {
        return branches.get(index);
    }

    public Branch getBranch(long index) {
        return NncUtils.find(branches, branch -> branch.getIndex() == index);
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
        for (Branch branch : branches) {
            if (branch.checkCondition(frame)) {
                if (branch.isNotEmpty()) {
                    frame.jumpTo(branch.getScope().getFirstNode());
                }
                break;
            }
        }
    }

}
