package tech.metavm.flow;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.flow.rest.UpdateObjectParamDTO;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.meta.ClassType;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;

import java.util.List;

@EntityType("更新对象节点")
public class UpdateObjectNode extends NodeRT<UpdateObjectParamDTO> {

    @EntityField("对象")
    private Value objectId;
    @EntityField("更新字段")
    private Table<UpdateField> fieldParams;

    public UpdateObjectNode(NodeDTO nodeDTO, UpdateObjectParamDTO param, ScopeRT scope) {
        super(nodeDTO, null, scope);
        setParam(param);
    }

    public Value getObjectId() {
        return objectId;
    }

    public List<UpdateField> getUpdateFields() {
        return fieldParams;
    }

    @Override
    protected void setParam(UpdateObjectParamDTO param) {
        objectId = ValueFactory.getValue(param.objectId(), getParsingContext());
        fieldParams = new Table<>(
                UpdateField.class,
                NncUtils.map(
                    param.fields(),
                    fieldParamDTO -> new UpdateField((ClassType) objectId.getType(), fieldParamDTO, getParsingContext())
                )
        );
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
