package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.common.ErrorCode;
import tech.metavm.entity.*;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.expression.ParsingContext;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.flow.rest.TryEndFieldDTO;
import tech.metavm.flow.rest.TryEndNodeParam;
import tech.metavm.flow.rest.TryEndValueDTO;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Klass;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.TypeParser;
import tech.metavm.util.BusinessException;
import tech.metavm.util.Instances;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EntityType("TryEnd节点")
public class TryEndNode extends ChildTypeNode {

    public static TryEndNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
        var node = (TryEndNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            var outputKlass = ((ClassType) TypeParser.parse(nodeDTO.outputType(), context)).resolve();
            node = new TryEndNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), outputKlass, (TryNode) prev, scope);
        }
        var param = (TryEndNodeParam) nodeDTO.getParam();
        if (param.fields().size() != node.getKlass().getReadyFields().size() - 1)
            throw new BusinessException(ErrorCode.NODE_FIELD_DEF_AND_FIELD_VALUE_MISMATCH, node.getName());
        var mergeFieldDTOs = param.fields();
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        var fields = new ArrayList<TryEndField>();
        Map<NodeRT, ParsingContext> raiseParsingContexts = new HashMap<>();
        for (TryEndFieldDTO mergeFieldDTO : mergeFieldDTOs) {
            List<TryEndValue> values = new ArrayList<>();
            for (TryEndValueDTO valueDTO : mergeFieldDTO.values()) {
                var raiseNode = context.getNode(Id.parse(valueDTO.raiseNodeId()));
                var raiseParsingContext = raiseParsingContexts.computeIfAbsent(raiseNode, k ->
                        FlowParsingContext.create(raiseNode.getScope(), raiseNode, context));
                values.add(
                        new TryEndValue(raiseNode, ValueFactory.create(valueDTO.value(), raiseParsingContext))
                );
            }
            fields.add(new TryEndField(
                    context.getField(Id.parse(mergeFieldDTO.fieldId())),
                    values,
                    ValueFactory.create(mergeFieldDTO.defaultValue(), parsingContext),
                    node
            ));
        }
        node.setFields(fields);
        return node;
    }

    @ChildEntity("字段列表")
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
