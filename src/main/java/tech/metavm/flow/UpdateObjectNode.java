package tech.metavm.flow;

import tech.metavm.flow.persistence.NodePO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.flow.rest.UpdateObjectParamDTO;
import tech.metavm.object.instance.Instance;
import tech.metavm.util.NncUtils;

import java.util.List;

public class UpdateObjectNode extends NodeRT<UpdateObjectParamDTO> {

    private Value objectId;
    private List<FieldParam> fieldParams;

    public UpdateObjectNode(NodeDTO nodeDTO, UpdateObjectParamDTO param, ScopeRT scope) {
        super(nodeDTO, null, scope);
        setParam(param);
    }


    public UpdateObjectNode(NodePO nodePO, UpdateObjectParamDTO param, ScopeRT scope) {
        super(nodePO, scope);
        setParam(param);
    }

    public Value getObjectId() {
        return objectId;
    }

    public List<FieldParam> getFieldParams() {
        return fieldParams;
    }

    @Override
    protected void setParam(UpdateObjectParamDTO param) {
        objectId = ValueFactory.getValue(param.objectId());
        fieldParams = NncUtils.map(
                param.fields(),
                fieldParamDTO -> new FieldParam(fieldParamDTO, getFlow().getContext())
        );
    }

    @Override
    protected UpdateObjectParamDTO getParam(boolean forPersistence) {
        return new UpdateObjectParamDTO(
                objectId.toDTO(),
                NncUtils.map(fieldParams, FieldParam::toDTO)
        );
    }

    @Override
    public void execute(FlowFrame frame) {
        long id = (long) objectId.evaluate(frame);
        Instance instance = frame.getInstance(id);
        if(instance != null) {
            for (FieldParam fieldParam : fieldParams) {
                Object value = fieldParam.evaluate(frame);
                instance.set(fieldParam.getField().getId(), value);
            }
        }
    }
}
