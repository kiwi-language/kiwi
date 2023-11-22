package tech.metavm.flow;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.expression.ParsingContext;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.flow.rest.UpdateFieldDTO;
import tech.metavm.flow.rest.UpdateObjectParamDTO;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.type.Field;
import tech.metavm.entity.ChildArray;
import tech.metavm.util.NncUtils;
import tech.metavm.entity.ReadonlyArray;

import java.util.Objects;

@EntityType("更新对象节点")
public class UpdateObjectNode extends NodeRT<UpdateObjectParamDTO> {

    public static UpdateObjectNode create(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope, IEntityContext entityContext) {
        UpdateObjectNode node = new UpdateObjectNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope);
        node.setParam(nodeDTO.getParam(), entityContext);
        return node;
    }

    @ChildEntity("对象")
    private Value objectId;
    
    @ChildEntity("更新字段")
    private final ChildArray<UpdateField> fieldParams = addChild(new ChildArray<>(UpdateField.class), "fieldParams");

    public UpdateObjectNode(Long tmpId, String name, NodeRT<?> prev, ScopeRT scope) {
        super(tmpId, name,  null, prev, scope);
    }

    public Value getObjectId() {
        return objectId;
    }

    public ReadonlyArray<UpdateField> getUpdateFields() {
        return fieldParams;
    }

    @Override
    protected void setParam(UpdateObjectParamDTO param, IEntityContext entityContext) {
        ParsingContext parsingContext = getParsingContext(entityContext);
        if(param.objectId() != null) {
            setObjectId(ValueFactory.create(param.objectId(), parsingContext));
        }
        if(param.fields() != null) {
            for (UpdateFieldDTO field : param.fields()) {
                NncUtils.requireTrue(
                        entityContext.getField(field.fieldRef()).getDeclaringType().isAssignableFrom(
                                getExpressionTypes().getType(objectId.getExpression())
                        )
                );
            }
            fieldParams.resetChildren(
                    NncUtils.map(
                            param.fields(),
                            updateFieldDTO -> saveField(updateFieldDTO, parsingContext, entityContext)
                    )
            );
        }
    }

    private UpdateField saveField(UpdateFieldDTO updateFieldDTO, ParsingContext parsingContext, IEntityContext entityContext) {
        var field = entityContext.getField(updateFieldDTO.fieldRef());
        var op = UpdateOp.getByCode(updateFieldDTO.opCode());
        var value = ValueFactory.create(updateFieldDTO.value(), parsingContext);
        var existing = fieldParams.get(UpdateField::getField, field);
        if(existing != null) {
            existing.setOp(op);
            existing.setValue(value);
            return existing;
        }
        else {
            return new UpdateField(field, op, value);
        }
    }

    void setField(long fieldId, UpdateOpAndValue opAndValue) {
        var field = fieldParams.get(Entity::getId, fieldId);
        field.setOp(opAndValue.op());
        field.setValue(opAndValue.value());
    }

    public void setObjectId(Value objectId) {
        this.objectId = addChild(objectId, "objectId");
    }


    public void setUpdateField(Field field, UpdateOp op, Value value) {
        var updateField = fieldParams.get(UpdateField::getField, field);
        if(updateField == null) {
            updateField = new UpdateField(field, op, value);
            fieldParams.addChild(updateField);
        }
        else {
            updateField.setOp(op);
            updateField.setValue(value);
        }
    }

    @Override
    protected UpdateObjectParamDTO getParam(boolean persisting) {
        return new UpdateObjectParamDTO(
                objectId.toDTO(persisting),
                NncUtils.map(fieldParams, fp -> fp.toDTO(persisting))
        );
    }

    @Override
    public void execute(MetaFrame frame) {
        ClassInstance instance = (ClassInstance) objectId.evaluate(frame);
        if(instance != null) {
            for (UpdateField updateField : fieldParams) {
                var inConstructor = getFlow().isConstructor() || Objects.equals(getFlow().getCode(), "<init>");
                updateField.execute(instance, frame, inConstructor, frame.getStack().getContext());
            }
        }
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitUpdateObjectNode(this);
    }
}
