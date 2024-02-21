package tech.metavm.flow;

import tech.metavm.entity.*;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.expression.ParsingContext;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.flow.rest.UpdateFieldDTO;
import tech.metavm.flow.rest.UpdateObjectNodeParam;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Field;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

@EntityType("更新对象节点")
public class UpdateObjectNode extends NodeRT {

    public static UpdateObjectNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext entityContext) {
        UpdateObjectNodeParam param = nodeDTO.getParam();
        ParsingContext parsingContext = FlowParsingContext.create(scope, prev, entityContext);
        var node = (UpdateObjectNode) entityContext.getNode(nodeDTO.getRef());
        var objectId = ValueFactory.create(param.objectId(), parsingContext);
        if (node == null) {
            node = new UpdateObjectNode(
                    nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), prev, scope, objectId
            );
        } else
            node.setObject(objectId);
        var type = (ClassType) node.getExpressionTypes().getType(objectId.getExpression());
        final var _node = node;
        node.setFields(NncUtils.map(
                param.fields(),
                updateFieldDTO -> saveField(_node, updateFieldDTO, parsingContext, type)
        ));
        return node;
    }

    private static UpdateField saveField(UpdateObjectNode node, UpdateFieldDTO updateFieldDTO, ParsingContext parsingContext, ClassType type) {
        var field = type.getField(updateFieldDTO.fieldRef());
        var op = UpdateOp.getByCode(updateFieldDTO.opCode());
        var value = ValueFactory.create(updateFieldDTO.value(), parsingContext);
        var existing = node.getField(field);
        if (existing != null) {
            existing.setOp(op);
            existing.setValue(value);
            return existing;
        } else {
            return new UpdateField(field, op, value);
        }
    }

    @ChildEntity("对象")
    private Value object;

    @ChildEntity("字段列表")
    private final ChildArray<UpdateField> fields = addChild(new ChildArray<>(UpdateField.class), "fields");

    public UpdateObjectNode(Long tmpId, String name, @Nullable String code, NodeRT prev, ScopeRT scope, Value object) {
        super(tmpId, name, code, null, prev, scope);
        this.object = addChild(object, "object");
    }

    public Value getObject() {
        return object;
    }

    public List<UpdateField> getFields() {
        return fields.toList();
    }

    public void setObject(Value object) {
        this.object = addChild(object, "object");
    }

    public void setUpdateField(Field field, UpdateOp op, Value value) {
        var updateField = fields.get(UpdateField::getField, field);
        if (updateField == null) {
            updateField = new UpdateField(field, op, value);
            fields.addChild(updateField);
        } else {
            updateField.setOp(op);
            updateField.setValue(value);
        }
    }

    @Override
    protected UpdateObjectNodeParam getParam(SerializeContext serializeContext) {
        return new UpdateObjectNodeParam(
                object.toDTO(),
                NncUtils.map(fields, UpdateField::toDTO)
        );
    }

    private void setFields(List<UpdateField> fields) {
        this.fields.resetChildren(fields);
    }

    public UpdateField getField(Field field) {
        return fields.get(UpdateField::getField, field);
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        try(var ignored = ContextUtil.getProfiler().enter("UpdateObjectNode.execute")) {
            ClassInstance instance = (ClassInstance) object.evaluate(frame);
            for (UpdateField updateField : fields) {
                var inConstructor = Flows.isConstructor(getFlow()) || Objects.equals(getFlow().getCode(), "<init>");
                updateField.execute(instance, frame, inConstructor);
            }
            return next(null);
        }
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("update("
                + object.getText()
                + ", {" + NncUtils.join(fields, UpdateField::getText, ", ") + "})");
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitUpdateObjectNode(this);
    }
}
