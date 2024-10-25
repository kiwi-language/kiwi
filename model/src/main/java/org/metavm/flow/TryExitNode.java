package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.common.ErrorCode;
import org.metavm.entity.ChildArray;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.expression.FlowParsingContext;
import org.metavm.expression.ParsingContext;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.flow.rest.TryExitFieldDTO;
import org.metavm.flow.rest.TryExitNodeParam;
import org.metavm.flow.rest.TryExitValueDTO;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Field;
import org.metavm.object.type.Klass;
import org.metavm.object.type.TypeParser;
import org.metavm.util.BusinessException;
import org.metavm.util.Instances;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.*;

@EntityType
public class TryExitNode extends ChildTypeNode {

    public static TryExitNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        var node = (TryExitNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            var outputKlass = ((ClassType) TypeParser.parseType(nodeDTO.outputType(), context)).resolve();
            node = new TryExitNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), outputKlass, (TryEnterNode) prev, scope);
        }
        var param = (TryExitNodeParam) nodeDTO.getParam();
        if (param.fields().size() != node.getKlass().getReadyFields().size() - 1)
            throw new BusinessException(ErrorCode.NODE_FIELD_DEF_AND_FIELD_VALUE_MISMATCH, node.getName());
        var mergeFieldDTOs = param.fields();
        var tryNode = (TryEnterNode) Objects.requireNonNull(prev);
        var defaultParsingContext = FlowParsingContext.create(tryNode.getBodyScope(), tryNode.getBodyScope().getLastNode(),
                context);
        var fields = new ArrayList<TryExitField>();
        Map<NodeRT, ParsingContext> raiseParsingContexts = new HashMap<>();
        for (TryExitFieldDTO fieldDTO : mergeFieldDTOs) {
            List<TryExitValue> values = new ArrayList<>();
            for (TryExitValueDTO valueDTO : fieldDTO.values()) {
                var raiseNode = context.getNode(Id.parse(valueDTO.raiseNodeId()));
                var raiseParsingContext = raiseParsingContexts.computeIfAbsent(raiseNode, k ->
                        FlowParsingContext.create(raiseNode.getScope(), raiseNode, context));
                values.add(
                        new TryExitValue(raiseNode, ValueFactory.create(valueDTO.value(), raiseParsingContext))
                );
            }
            fields.add(new TryExitField(
                    context.getField(Id.parse(fieldDTO.fieldId())),
                    values,
                    ValueFactory.create(fieldDTO.defaultValue(), defaultParsingContext),
                    node
            ));
        }
        node.setFields(fields);
        return node;
    }

    @ChildEntity
    private final ChildArray<TryExitField> fields = addChild(new ChildArray<>(TryExitField.class), "fields");

    public TryExitNode(Long tmpId, String name, @Nullable String code, Klass outputType, TryEnterNode previous, ScopeRT scope) {
        super(tmpId, name, code, outputType, previous, scope);
    }

    @Override
    protected TryExitNodeParam getParam(SerializeContext serializeContext) {
        return new TryExitNodeParam(NncUtils.map(fields, TryExitField::toDTO));
    }

    @Override
    @NotNull
    public TryEnterNode getPredecessor() {
        return (TryEnterNode) NncUtils.requireNonNull(super.getPredecessor());
    }

    public void addField(TryExitField field) {
        this.fields.addChild(field);
    }

    public List<TryExitField> getFields() {
        return fields.toList();
    }

    public void setFields(ArrayList<TryExitField> fields) {
        this.fields.resetChildren(fields);
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        var tryNode = frame.exitTrySection();
        assert tryNode == getPredecessor();
        var exceptionInfo = frame.getExceptionInfo(getPredecessor());
        var exceptionField = getKlass().getFieldByCode("exception");
        Value exception;
        NodeRT raiseNode;
        if (exceptionInfo != null) {
            exception = exceptionInfo.exception().getReference();
            raiseNode = exceptionInfo.raiseNode();
        } else {
            exception = Instances.nullInstance();
            raiseNode = null;
        }
        Map<Field, Value> fieldValues = new HashMap<>(NncUtils.toMap(
                fields, TryExitField::getField,
                f -> f.getValue(raiseNode).evaluate(frame)
        ));
        fieldValues.put(exceptionField, exception);
        return next(ClassInstance.create(fieldValues, getType()).getReference());
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("tryEnd {" + NncUtils.join(fields, TryExitField::getText, ", ") + "}");
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitTryEndNode(this);
    }
}
