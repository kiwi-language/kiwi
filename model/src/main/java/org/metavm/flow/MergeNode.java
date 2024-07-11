package org.metavm.flow;

import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.common.ErrorCode;
import org.metavm.entity.*;
import org.metavm.expression.ExpressionTypeMap;
import org.metavm.expression.FlowParsingContext;
import org.metavm.expression.ParsingContext;
import org.metavm.flow.rest.MergeNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Field;
import org.metavm.object.type.Klass;
import org.metavm.object.type.TypeParser;
import org.metavm.util.BusinessException;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.*;

@EntityType
public class MergeNode extends ChildTypeNode {

    public static MergeNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
        var outputKlass = ((ClassType) TypeParser.parseType(nodeDTO.outputType(), context)).resolve();
        var branchNode = (BranchNode) Objects.requireNonNull(prev);
        var node = (MergeNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null)
            node = new MergeNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), branchNode, outputKlass, scope);
        var param = (MergeNodeParam) nodeDTO.param();
        if (param.fields().size() != node.getKlass().getReadyFields().size())
            throw new BusinessException(ErrorCode.MISSING_MERGE_NODE_FIELD_VALUE);
        var mergeFields = new ArrayList<MergeNodeField>();
        for (var mergeFieldDTO : param.fields()) {
            var field = context.getField(Id.parse(mergeFieldDTO.fieldId()));
            var mergeField = node.findMergeField(field);
            if (mergeField == null)
                mergeField = new MergeNodeField(field, node);
            var branchParsingContexts = new HashMap<Branch, ParsingContext>();
            for (var value : mergeFieldDTO.values()) {
                var branch = Objects.requireNonNull(context.getEntity(Branch.class, Id.parse(value.branchId())));
                if (branch.getOwner() != branchNode)
                    throw new InternalException("Branch " + branch + " doesn't belong to the branch node of this merge node");
                mergeField.setValue(branch,
                        ValueFactory.create(value.value(),
                                branchParsingContexts.computeIfAbsent(branch,
                                        k -> FlowParsingContext.create(branch.getScope(), branch.getScope().getLastNode(), context)
                                )
                        ));
            }
            mergeFields.add(mergeField);
        }
        node.setFields(mergeFields);
        node.mergeExpressionTypes(getExpressionTypeMap(branchNode));
        return node;
    }

    public static ExpressionTypeMap getExpressionTypeMap(BranchNode branchNode) {
        ExpressionTypeMap expressionTypes = null;
        for (Branch branch : branchNode.getBranches()) {
            var lastNode = branch.getScope().getLastNode();
            if (lastNode == null || !lastNode.isExit()) {
                var newExprTypes = lastNode == null ?
                        branch.getScope().getExpressionTypes() : lastNode.getExpressionTypes();
                if (expressionTypes == null) {
                    expressionTypes = newExprTypes;
                } else {
                    expressionTypes = expressionTypes.union(newExprTypes);
                }
            }
        }
        return expressionTypes != null ? expressionTypes : ExpressionTypeMap.EMPTY;
    }

    @ChildEntity
    private final ReadWriteArray<Branch> branches = addChild(new ReadWriteArray<>(Branch.class), "branches");

    @ChildEntity
    private final ChildArray<MergeNodeField> fields = addChild(new ChildArray<>(MergeNodeField.class), "fields");

    public MergeNode(Long tmpId, String name, @Nullable String code, BranchNode branchNode, Klass outputType, ScopeRT scope) {
        super(tmpId, name, code, outputType, branchNode, scope);
    }

    public void addField(MergeNodeField field) {
        if (fields.get(MergeNodeField::getField, field.getField()) != null) {
            throw new InternalException("Field " + field.getField() + " is already added");
        }
        fields.addChild(field);
    }

    @Override
    protected MergeNodeParam getParam(SerializeContext serializeContext) {
        return new MergeNodeParam(
                NncUtils.map(fields, MergeNodeField::toDTO)
        );
    }

    public void addBranch(Branch branch) {
        NncUtils.requireFalse(branches.contains(branch));
        branches.add(branch);
    }

    public void setFields(List<MergeNodeField> fields) {
        this.fields.resetChildren(fields);
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
        return next(ClassInstance.create(fieldValues, getType()).getReference());
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("merge {" + NncUtils.join(fields, MergeNodeField::getText, ", ") + "}");
    }

    @Override
    protected List<Object> nodeBeforeRemove() {
        if (getPredecessor() instanceof BranchNode branchNode) {
            return List.of(branchNode);
        } else {
            return List.of();
        }
    }

    public List<NodeRT> getGlobalPredecessors() {
        var branchNode = getBranchNode();
        List<NodeRT> predecessors = NncUtils.mapAndFilter(
                branchNode.getBranches(),
                branch -> branch.getScope().getLastNode(),
                Objects::nonNull
        );
        return !predecessors.isEmpty() ? predecessors : List.of(branchNode);
    }

    public List<MergeNodeField> getFields() {
        return fields.toList();
    }

    public @Nullable MergeNodeField findMergeField(Field field) {
        return fields.get(MergeNodeField::getField, field);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitMergeNode(this);
    }
}
