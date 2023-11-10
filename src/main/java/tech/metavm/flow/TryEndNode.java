package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.dto.ErrorCode;
import tech.metavm.entity.ChildArray;
import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.expression.ParsingContext;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.flow.rest.TryEndParamDTO;
import tech.metavm.flow.rest.TryEndFieldDTO;
import tech.metavm.flow.rest.TryEndValueDTO;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.util.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EntityType("尝试结束节点")
public class TryEndNode extends ChildTypeNode<TryEndParamDTO> {

    public static TryEndNode create(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope, IEntityContext context) {
        var node = new TryEndNode(nodeDTO.tmpId(), nodeDTO.name(),
                context.getClassType(nodeDTO.outputTypeRef()), (TryNode) prev, scope);
        node.setParam(nodeDTO.getParam(), context);
        return node;
    }

    @ChildEntity("字段列表")
    private final ChildArray<TryEndField> fields = addChild(new ChildArray<>(TryEndField.class), "fields");

    public TryEndNode(Long tmpId, String name, ClassType outputType, TryNode previous, ScopeRT scope) {
        super(tmpId, name, outputType, previous, scope);
    }

    @Override
    protected TryEndParamDTO getParam(boolean persisting) {
        return new TryEndParamDTO(NncUtils.map(fields, TryEndField::toDTO));
    }

    @Override
    @NotNull
    public TryNode getPredecessor() {
        return (TryNode) NncUtils.requireNonNull(super.getPredecessor());
    }

    @Override
    protected void setParam(TryEndParamDTO param, IEntityContext context) {
        if (param.fields() != null) {
            if (param.fields().size() != getType().getFields().size() - 1) {
                throw new BusinessException(ErrorCode.NODE_FIELD_DEF_AND_FIELD_VALUE_MISMATCH, this.getName());
            }
            this.fields.clear();
            var mergeFieldDTOs = param.fields();
            var parsingContext = getParsingContext(context);
            Map<NodeRT<?>, ParsingContext> raiseParsingContexts = new HashMap<>();
            for (TryEndFieldDTO mergeFieldDTO : mergeFieldDTOs) {
                List<TryEndValue> values = new ArrayList<>();
                for (TryEndValueDTO valueDTO : mergeFieldDTO.values()) {
                    var raiseNode = context.getNode(valueDTO.raiseNodeRef());
                    var raiseParsingContext = raiseParsingContexts.computeIfAbsent(raiseNode, k ->
                            new FlowParsingContext(raiseNode.getScope(), raiseNode, context.getInstanceContext()));
                    values.add(
                            new TryEndValue(raiseNode, ValueFactory.create(valueDTO.value(), raiseParsingContext))
                    );
                }
                new TryEndField(
                        context.getField(mergeFieldDTO.fieldRef()),
                        values,
                        ValueFactory.create(mergeFieldDTO.defaultValue(), parsingContext),
                        this
                );
            }
        }
    }

    public void addField(TryEndField field) {
        this.fields.addChild(field);
    }

    public List<TryEndField> getFields() {
        return fields.toList();
    }

    @Override
    public void execute(MetaFrame frame) {
        NncUtils.requireTrue(frame.exitTrySection() == getPredecessor());
        var exceptionInfo = frame.getExceptionInfo(getPredecessor());
        var exceptionField = getType().getFieldByCode("exception");
        Instance exception;
        NodeRT<?> raiseNode;
        if (exceptionInfo != null) {
            exception = exceptionInfo.exception();
            raiseNode = exceptionInfo.raiseNode();
        } else {
            exception = InstanceUtils.nullInstance();
            raiseNode = null;
        }
        Map<Field, Instance> fieldValues = new HashMap<>(NncUtils.toMap(
                fields, TryEndField::getField,
                f -> f.getValue(raiseNode).evaluate(frame)
        ));
        fieldValues.put(exceptionField, exception);
        frame.setResult(new ClassInstance(fieldValues, getType()));
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitTryEndNode(this);
    }
}
