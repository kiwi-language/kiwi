package tech.metavm.flow;

import tech.metavm.autograph.ExpressionTypeMap;
import tech.metavm.dto.ErrorCode;
import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.expression.ParsingContext;
import tech.metavm.flow.rest.MergeParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.util.BusinessException;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;

import java.util.*;

@EntityType("合并节点")
public class MergeNode extends NodeRT<MergeParamDTO> {

    public static MergeNode create(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope, IEntityContext context) {
        var outputType = context.getEntity(ClassType.class, nodeDTO.outputTypeRef());
        var branchNode = (BranchNode) prev;
        var node = new MergeNode(nodeDTO.tmpId(), nodeDTO.name(), branchNode, outputType, scope);
        node.setParam(nodeDTO.getParam(), context);
//        ExpressionTypeMap expressionTypes = getExpressionTypeMap(branchNode);
//        node.mergeExpressionTypes(expressionTypes);
        return node;
    }

//    public static ExpressionTypeMap getExpressionTypeMap(BranchNode branchNode) {
//        ExpressionTypeMap expressionTypes = null;
//        for (Branch branch : branchNode.getBranches()) {
//            var lastNode = branch.getScope().getLastNode();
//            if(lastNode == null || !lastNode.isExit()) {
//                var newExprTypes = lastNode == null ?
//                        branch.getScope().getExpressionTypes() : lastNode.getExpressionTypes();
//                if(expressionTypes == null) {
//                    expressionTypes = newExprTypes;
//                }
//                else {
//                    expressionTypes = expressionTypes.union(newExprTypes);
//                }
//            }
//        }
//        return expressionTypes != null ? expressionTypes : ExpressionTypeMap.EMPTY;
//    }

    @ChildEntity("分支列表")
    private final Table<Branch> branches = new Table<>(Branch.class, false);

    @ChildEntity("字段列表")
    private final Table<MergeNodeField> fields = new Table<>(MergeNodeField.class, true);

    public MergeNode(Long tmpId, String name, BranchNode branchNode, ClassType outputType, ScopeRT scope) {
        super(tmpId, name, outputType, branchNode, scope);
    }

    public void addField(MergeNodeField field) {
        if (fields.get(MergeNodeField::getField, field.getField()) != null) {
            throw new InternalException("Field " + field.getField() + " is already added");
        }
        fields.add(field);
    }

    @Override
    protected MergeParamDTO getParam(boolean persisting) {
        return new MergeParamDTO(
                NncUtils.map(fields, f -> f.toDTO(persisting))
        );
    }

    public void addBranch(Branch branch) {
        NncUtils.requireFalse(branches.contains(branch));
        branches.add(branch);
    }

    @Override
    protected void setParam(MergeParamDTO param, IEntityContext context) {
        if (param.fields() != null) {
            if (param.fields().size() != getType().getFields().size()) {
                throw new BusinessException(ErrorCode.MISSING_MERGE_NODE_FIELD_VALUE);
            }
            fields.clear();
            var branchNode = getPredecessor();
            Map<Branch, ParsingContext> branchParsingContexts = new HashMap<>();
            for (var fieldDTO : param.fields()) {
                MergeNodeField field = new MergeNodeField(context.getField(fieldDTO.fieldRef()), this);
                for (var value : fieldDTO.values()) {
                    Branch branch = context.getEntity(Branch.class, value.branchRef());
                    if (branch.getOwner() != branchNode) {
                        throw new InternalException("Branch " + branch + " doesn't belong to the branch node of this merge node");
                    }
                    field.setValue(branch,
                            ValueFactory.create(value.value(),
                                    branchParsingContexts.computeIfAbsent(branch,
                                            k -> new FlowParsingContext(
                                                    branch.getScope(), branch.getScope().getLastNode(),
                                                    context.getInstanceContext())
                                            )
                                    ));
                }
            }
        }
    }

    @Override
    public ClassType getType() {
        return (ClassType) super.getType();
    }

    public BranchNode getBranchNode() {
        return (BranchNode) getPredecessor();
    }

    @Override
    public void execute(FlowFrame frame) {
        Branch branch = frame.getSelectedBranch(getBranchNode());
        Map<Field, Instance> fieldValues = new HashMap<>();
        for (MergeNodeField field : fields) {
            fieldValues.put(
                    field.getField(),
                    field.getValue(branch).evaluate(frame)
            );
        }
        frame.setResult(new ClassInstance(fieldValues, getType()));
    }

    @Override
    protected List<Object> nodeBeforeRemove() {
        if (getPredecessor() instanceof BranchNode branchNode) {
            return List.of(branchNode, getType());
        }
        else {
            return List.of(getType());
        }
    }

    public List<NodeRT<?>> getGlobalPredecessors() {
        var branchNode = getBranchNode();
        List<NodeRT<?>> predecessors = NncUtils.mapAndFilter(
                branchNode.getBranches(),
                branch -> branch.getScope().getLastNode(),
                Objects::nonNull
        );
        return !predecessors.isEmpty() ? predecessors : List.of(branchNode);
    }

    public List<MergeNodeField> getFields() {
        return Collections.unmodifiableList(fields);
    }
}
