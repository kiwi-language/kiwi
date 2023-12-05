package tech.metavm.flow;

import tech.metavm.common.ErrorCode;
import tech.metavm.entity.*;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.expression.ExpressionTypeMap;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.expression.ParsingContext;
import tech.metavm.flow.rest.MergeParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Field;
import tech.metavm.util.*;

import java.util.*;

@EntityType("合并节点")
public class MergeNode extends ChildTypeNode<MergeParamDTO> {

    public static MergeNode create(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope, IEntityContext context) {
        var outputType = context.getEntity(ClassType.class, nodeDTO.outputTypeRef());
        var branchNode = (BranchNode) prev;
        var node = new MergeNode(nodeDTO.tmpId(), nodeDTO.name(), branchNode, outputType, scope);
        node.setParam(nodeDTO.getParam(), context);
        ExpressionTypeMap expressionTypes = getExpressionTypeMap(branchNode);
        node.mergeExpressionTypes(expressionTypes);
        return node;
    }

    public static ExpressionTypeMap getExpressionTypeMap(BranchNode branchNode) {
        ExpressionTypeMap expressionTypes = null;
        for (Branch branch : branchNode.getBranches()) {
            var lastNode = branch.getScope().getLastNode();
            if(lastNode == null || !lastNode.isExit()) {
                var newExprTypes = lastNode == null ?
                        branch.getScope().getExpressionTypes() : lastNode.getExpressionTypes();
                if(expressionTypes == null) {
                    expressionTypes = newExprTypes;
                }
                else {
                    expressionTypes = expressionTypes.union(newExprTypes);
                }
            }
        }
        return expressionTypes != null ? expressionTypes : ExpressionTypeMap.EMPTY;
    }

    @ChildEntity("分支列表")
    private final ReadWriteArray<Branch> branches = addChild(new ReadWriteArray<>(Branch.class), "branches");

    @ChildEntity("字段列表")
    private final ChildArray<MergeNodeField> fields = addChild(new ChildArray<>(MergeNodeField.class), "fields");

    public MergeNode(Long tmpId, String name, BranchNode branchNode, ClassType outputType, ScopeRT scope) {
        super(tmpId, name, outputType, branchNode, scope);
    }

    public void addField(MergeNodeField field) {
        if (fields.get(MergeNodeField::getField, field.getField()) != null) {
            throw new InternalException("Field " + field.getField() + " is already added");
        }
        fields.addChild(field);
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
            if (param.fields().size() != getType().getReadyFields().size()) {
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

    public BranchNode getBranchNode() {
        return (BranchNode) getPredecessor();
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        Branch branch = frame.getSelectedBranch(getBranchNode());
        Map<Field, Instance> fieldValues = new HashMap<>();
        for (MergeNodeField field : fields) {
            fieldValues.put(
                    field.getField(),
                    field.getValue(branch).evaluate(frame)
            );
        }
        return next(new ClassInstance(fieldValues, getType()));
    }

    @Override
    protected List<Object> nodeBeforeRemove() {
        if (getPredecessor() instanceof BranchNode branchNode) {
            return List.of(branchNode);
        }
        else {
            return List.of();
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

    public ReadonlyArray<MergeNodeField> getFields() {
        return fields;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitMergeNode(this);
    }
}
