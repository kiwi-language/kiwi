package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.common.ErrorCode;
import org.metavm.entity.*;
import org.metavm.expression.FlowParsingContext;
import org.metavm.expression.ParsingContext;
import org.metavm.flow.rest.JoinNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Field;
import org.metavm.object.type.Klass;
import org.metavm.object.type.TypeParser;
import org.metavm.util.BusinessException;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.*;

@EntityType
public class JoinNode extends ChildTypeNode {

    public static JoinNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        var outputKlass = ((ClassType) TypeParser.parseType(nodeDTO.outputType(), context)).resolve();
        var node = (JoinNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null)
            node = new JoinNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), outputKlass, prev, scope);
        var param = (JoinNodeParam) nodeDTO.param();
        if (param.fields().size() != node.getKlass().getReadyFields().size())
            throw new BusinessException(ErrorCode.MISSING_MERGE_NODE_FIELD_VALUE);
        if(stage == NodeSavingStage.FINALIZE) {
            var fields = new ArrayList<JoinNodeField>();
            for (var fieldDTO : param.fields()) {
                var field = context.getField(Id.parse(fieldDTO.fieldId()));
                var joinField = new JoinNodeField(field, node);
                var branchParsingContexts = new HashMap<NodeRT, ParsingContext>();
                for (var value : fieldDTO.values()) {
                    var sourceNode = Objects.requireNonNull(context.getNode(Id.parse(value.sourceNodeId())));
                    joinField.setValue(sourceNode,
                            ValueFactory.create(value.value(),
                                    branchParsingContexts.computeIfAbsent(sourceNode,
                                            k -> FlowParsingContext.create(sourceNode.getScope(), sourceNode, context)
                                    )
                            ));
                }
                fields.add(joinField);
            }
            node.setFields(fields);
            node.setSources(
                    NncUtils.map(
                            param.sourceIds(),
                            sourceId -> Objects.requireNonNull(context.getEntity(JumpNode.class, sourceId))
                    )
            );
        }
        return node;
    }

    @ChildEntity
    private final ReadWriteArray<NodeRT> sources = addChild(new ReadWriteArray<>(NodeRT.class), "sources");

    @ChildEntity
    private final ChildArray<JoinNodeField> fields = addChild(new ChildArray<>(JoinNodeField.class), "fields");

    public JoinNode(Long tmpId, @NotNull String name, @Nullable String code, Klass klass, @Nullable NodeRT previous, @NotNull ScopeRT scope) {
        super(tmpId, name, code, klass, previous, scope);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitJoinNode(this);
    }

    @Override
    protected JoinNodeParam getParam(SerializeContext serializeContext) {
        return new JoinNodeParam(
                NncUtils.map(sources, serializeContext::getStringId),
                NncUtils.map(fields, f -> f.toDTO(serializeContext))
        );
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        var source = Objects.requireNonNull(frame.getLastNode());
        Map<Field, Value> fieldValues = new HashMap<>();
        for (var field : fields) {
            fieldValues.put(
                    field.getField(),
                    field.getValue(source).evaluate(frame)
            );
        }
        return next(ClassInstance.create(fieldValues, getType()).getReference());
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("merge {" + NncUtils.join(fields, JoinNodeField::getText, ", ") + "}");
    }

    public void addField(JoinNodeField field) {
        fields.addChild(field);
    }

    private void setFields(ArrayList<JoinNodeField> fields) {
        this.fields.resetChildren(fields);
    }

    public void addSource(NodeRT source) {
        sources.add(source);
        unionExpressionTypes(source.getExpressionTypes());
    }

    public void setSources(List<NodeRT> sources) {
        this.sources.reset(sources);
    }

    public List<NodeRT> getSources() {
        return sources.toList();
    }

    public List<JoinNodeField> getFields() {
        return fields.toList();
    }
}
