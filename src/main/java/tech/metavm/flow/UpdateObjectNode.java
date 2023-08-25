package tech.metavm.flow;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.expression.Expression;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.flow.rest.UpdateObjectParamDTO;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.expression.ParsingContext;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;

import java.util.List;

@EntityType("更新对象节点")
public class UpdateObjectNode extends NodeRT<UpdateObjectParamDTO> {

    public static UpdateObjectNode create(NodeDTO nodeDTO, IEntityContext entityContext) {
        UpdateObjectNode node = new UpdateObjectNode(nodeDTO, entityContext.getScope(nodeDTO.scopeId()));
        node.setParam(nodeDTO.getParam(), entityContext);
        return node;
    }

    @ChildEntity("对象")
    private Value objectId;
    @ChildEntity("更新字段")
    private final Table<UpdateField> fieldParams = new Table<>(UpdateField.class, true);

    public UpdateObjectNode(NodeDTO nodeDTO, ScopeRT scope) {
        super(nodeDTO, null, scope);
    }

    public UpdateObjectNode(String name, NodeRT<?> prev, ScopeRT scope) {
        super(name, NodeKind.UPDATE_OBJECT, null, prev, scope);
    }

    public Value getObjectId() {
        return objectId;
    }

    public List<UpdateField> getUpdateFields() {
        return fieldParams;
    }

    @Override
    protected void setParam(UpdateObjectParamDTO param, IEntityContext entityContext) {
        ParsingContext parsingContext = getParsingContext(entityContext);
        objectId = ValueFactory.getValue(param.objectId(), parsingContext);
        fieldParams.clear();
        fieldParams.addAll(
                NncUtils.map(
                    param.fields(),
                    fieldParamDTO -> new UpdateField((ClassType) objectId.getType(), fieldParamDTO, parsingContext)
                )
        );
    }

    void setField(long fieldId, UpdateOpAndValue opAndValue) {
        var field = fieldParams.get(Entity::getId, fieldId);
        field.setOp(opAndValue.op());
        field.setValue(opAndValue.value());
    }

    public void setObjectId(Expression expression) {
        this.objectId = new ExpressionValue(expression);
    }


    public void setUpdateField(Field field, Expression value) {
        var updateField = fieldParams.get(UpdateField::getField, field);
        ExpressionValue exprValue = new ExpressionValue(value);
        if(updateField == null) {
            updateField = new UpdateField(field, UpdateOp.SET, exprValue);
            fieldParams.add(updateField);
        }
        else {
            updateField.setOp(UpdateOp.SET);
            updateField.setValue(exprValue);
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
    public void execute(FlowFrame frame) {
        ClassInstance instance = (ClassInstance) objectId.evaluate(frame);
        if(instance != null) {
            for (UpdateField updateField : fieldParams) {
                updateField.execute(instance, frame, frame.getStack().getContext());
            }
        }
    }
}
