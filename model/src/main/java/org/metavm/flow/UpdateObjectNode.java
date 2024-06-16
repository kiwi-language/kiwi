package org.metavm.flow;

import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.entity.*;
import org.metavm.expression.FlowParsingContext;
import org.metavm.expression.ParsingContext;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.flow.rest.UpdateFieldDTO;
import org.metavm.flow.rest.UpdateObjectNodeParam;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Field;
import org.metavm.object.type.Klass;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

@EntityType
public class UpdateObjectNode extends NodeRT {

    public static UpdateObjectNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext entityContext) {
        UpdateObjectNodeParam param = nodeDTO.getParam();
        ParsingContext parsingContext = FlowParsingContext.create(scope, prev, entityContext);
        var node = (UpdateObjectNode) entityContext.getNode(Id.parse(nodeDTO.id()));
        var objectId = ValueFactory.create(param.objectId(), parsingContext);
        if (node == null) {
            node = new UpdateObjectNode(
                    nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), prev, scope, objectId,
                    List.of());
        } else
            node.setObject(objectId);
        var type = (ClassType) node.getExpressionTypes().getType(objectId.getExpression());
        var klass = type.resolve();
        final var _node = node;
        node.setFields(NncUtils.map(
                param.fields(),
                updateFieldDTO -> saveField(_node, updateFieldDTO, parsingContext, klass)
        ));
        return node;
    }

    private static UpdateField saveField(UpdateObjectNode node, UpdateFieldDTO updateFieldDTO, ParsingContext parsingContext, Klass type) {
        var field = updateFieldDTO.getField(type);
        var op = UpdateOp.getByCode(updateFieldDTO.opCode());
        var value = ValueFactory.create(updateFieldDTO.value(), parsingContext);
        var existing = node.getField(field);
        if (existing != null) {
            existing.setOp(op);
            existing.setValue(value);
            return existing;
        } else {
            return new UpdateField(field.getRef(), op, value);
        }
    }

    private Value object;

    @ChildEntity
    private final ChildArray<UpdateField> fields = addChild(new ChildArray<>(UpdateField.class), "fields");

    public UpdateObjectNode(Long tmpId, String name, @Nullable String code, NodeRT prev, ScopeRT scope, Value object, List<UpdateField> fields) {
        super(tmpId, name, code, null, prev, scope);
        this.object = object;
        setFields(fields);
    }

    public Value getObject() {
        return object;
    }

    public List<UpdateField> getFields() {
        return fields.toList();
    }

    public void setObject(Value object) {
        this.object = object;
    }

    public void setUpdateField(Field field, UpdateOp op, Value value) {
        var updateField = fields.get(f -> f.getFieldRef().resolve(), field);
        if (updateField == null) {
            updateField = new UpdateField(field.getRef(), op, value);
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
        var selfType = object.getType();
        for (UpdateField field : fields) {
            if (!field.getFieldRef().getDeclaringType().isAssignableFrom(selfType))
                throw new InternalException("Field " + field.getFieldRef().resolve() + " is not defined in klass " + selfType);
        }
        this.fields.resetChildren(fields);
    }

    public UpdateField getField(Field field) {
        return fields.get(UpdateField::getFieldRef, field);
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        ClassInstance instance = (ClassInstance) object.evaluate(frame);
        for (UpdateField updateField : fields) {
            var inConstructor = Flows.isConstructor(getFlow()) || Objects.equals(getFlow().getCode(), "<init>");
            updateField.execute(instance, frame, inConstructor);
        }
        return next(null);
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
