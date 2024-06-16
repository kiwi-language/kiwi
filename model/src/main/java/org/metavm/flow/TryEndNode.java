package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.common.ErrorCode;
import org.metavm.entity.*;
import org.metavm.expression.FlowParsingContext;
import org.metavm.expression.ParsingContext;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.flow.rest.TryEndFieldDTO;
import org.metavm.flow.rest.TryEndNodeParam;
import org.metavm.flow.rest.TryEndValueDTO;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Field;
import org.metavm.object.type.Klass;
import org.metavm.object.type.TypeParser;
import org.metavm.util.BusinessException;
import org.metavm.util.Instances;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EntityType
public class TryEndNode extends ChildTypeNode {

    public static TryEndNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
        var node = (TryEndNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            var outputKlass = ((ClassType) TypeParser.parseType(nodeDTO.outputType(), context)).resolve();
            node = new TryEndNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), outputKlass, (TryNode) prev, scope);
        }
        var param = (TryEndNodeParam) nodeDTO.getParam();
        if (param.fields().size() != node.getKlass().getReadyFields().size() - 1)
            throw new BusinessException(ErrorCode.NODE_FIELD_DEF_AND_FIELD_VALUE_MISMATCH, node.getName());
        var mergeFieldDTOs = param.fields();
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        var fields = new ArrayList<TryEndField>();
        Map<NodeRT, ParsingContext> raiseParsingContexts = new HashMap<>();
        for (TryEndFieldDTO fieldDTO : mergeFieldDTOs) {
            List<TryEndValue> values = new ArrayList<>();
            for (TryEndValueDTO valueDTO : fieldDTO.values()) {
                var raiseNode = context.getNode(Id.parse(valueDTO.raiseNodeId()));
                var raiseParsingContext = raiseParsingContexts.computeIfAbsent(raiseNode, k ->
                        FlowParsingContext.create(raiseNode.getScope(), raiseNode, context));
                values.add(
                        new TryEndValue(raiseNode, ValueFactory.create(valueDTO.value(), raiseParsingContext))
                );
            }
            fields.add(new TryEndField(
                    context.getField(Id.parse(fieldDTO.fieldId())),
                    values,
                    ValueFactory.create(fieldDTO.defaultValue(), parsingContext),
                    node
            ));
        }
        node.setFields(fields);
        return node;
    }

    @ChildEntity
    private final ChildArray<TryEndField> fields = addChild(new ChildArray<>(TryEndField.class), "fields");

    public TryEndNode(Long tmpId, String name, @Nullable String code, Klass outputType, TryNode previous, ScopeRT scope) {
        super(tmpId, name, code, outputType, previous, scope);
    }

    @Override
    protected TryEndNodeParam getParam(SerializeContext serializeContext) {
        return new TryEndNodeParam(NncUtils.map(fields, TryEndField::toDTO));
    }

    @Override
    @NotNull
    public TryNode getPredecessor() {
        return (TryNode) NncUtils.requireNonNull(super.getPredecessor());
    }

    public void addField(TryEndField field) {
        this.fields.addChild(field);
    }

    public List<TryEndField> getFields() {
        return fields.toList();
    }

    public void setFields(ArrayList<TryEndField> fields) {
        this.fields.resetChildren(fields);
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        var tryNode = frame.exitTrySection();
        assert tryNode == getPredecessor();
        var exceptionInfo = frame.getExceptionInfo(getPredecessor());
        var exceptionField = getKlass().getFieldByCode("exception");
        Instance exception;
        NodeRT raiseNode;
        if (exceptionInfo != null) {
            exception = exceptionInfo.exception();
            raiseNode = exceptionInfo.raiseNode();
        } else {
            exception = Instances.nullInstance();
            raiseNode = null;
        }
        Map<Field, Instance> fieldValues = new HashMap<>(NncUtils.toMap(
                fields, TryEndField::getField,
                f -> f.getValue(raiseNode).evaluate(frame)
        ));
        fieldValues.put(exceptionField, exception);
        return next(ClassInstance.create(fieldValues, getType()));
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("tryEnd {" + NncUtils.join(fields, TryEndField::getText, ", ") + "}");
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitTryEndNode(this);
    }
}
